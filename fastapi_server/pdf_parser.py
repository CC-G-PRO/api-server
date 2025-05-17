import pdfplumber
import re
from enum import Enum, auto
import json

class Section(Enum):
    NONE = auto()
    BASIC_INFO = auto()
    GRADUATION_INFO = auto()
    GENERAL_EDUCATION = auto()
    MAJOR_EDUCATION = auto()
    MAJOR_REQUIREMENT = auto()
    COURSE_DETAILS = auto()

def parse_text_file(lines : list[str]) -> dict:
    '''
    줄바꿈으로 구분된 텍스트 형식의 데이터의 Section 을 나누어 diction 형태로 반환
    '''

    sections = {
        Section.BASIC_INFO: [],
        Section.GRADUATION_INFO: [],
        Section.GENERAL_EDUCATION: [],
        Section.MAJOR_EDUCATION: [],
        Section.MAJOR_REQUIREMENT: [],
        Section.COURSE_DETAILS: []
    }

    current_section = Section.NONE
    passing_line = 0 

    for line in lines:
        line = line.strip()
        if re.search(r'학\s*번', line) and re.search(r'성\s*명', line):
            current_section = Section.BASIC_INFO

        if re.search(r'졸업판정.*한국어능력인증', line):
            current_section = Section.GRADUATION_INFO
            passing_line = 1

        elif re.search(r'교양내역', line):
            current_section = Section.GENERAL_EDUCATION
            passing_line = 1

        elif re.search(r'전공내역', line):
            current_section = Section.MAJOR_EDUCATION
            passing_line = 1

        elif re.search(r'교양/기타/금학기수강학점', line):
            break

        if(passing_line != 0):
            passing_line -= 1
            continue

        if current_section != Section.NONE:
            sections[current_section].append(line)

    return sections

def parse_student_info(lines: list[str]) -> dict:
    result = {}

    #key 에 해당하는 데이터들
    key_order = [ 
        ("학 번", "student_number"),
        ("성 명", "student_name"),
        ("학 과", "department"),
        ("학 년", "grade"),
        ("등록횟수", "enroll_semester"),
        ("학적상태", "status"),
        ("입 학 구 분", "enroll_division"),
        ("졸업기준학점(년도)", "curriculum_year"),
        ("외국인여부", "is_foreign"),
        ("최종변동", "latest_modified"),
        ("최종판정", "fianl_judgment"),
    ]

    full_text = ' '.join(lines)

    for raw_key, clean_key in key_order:
        pattern = rf'{re.escape(raw_key)}\s+([^\s]+)'
        match = re.search(pattern, full_text)
        if match:
            result[clean_key] = match.group(1)

    match = re.search(r'자료생성일시\s+대상자\s+([0-9:/\s]+)', full_text)
    if match:
        result["evaluation_date"] = match.group(1).strip()

    match = re.search(r'이수과목\s+([0-9:/\s]+)', full_text)
    if match:
        result["reference_date_completed_courses"] = match.group(1).strip()

    match = re.search(r'사정처리\s+([0-9:/\s]+)', full_text)
    if match:
        result["data_printed_date"] = match.group(1).strip()

    return result
def parse_graduation_data(lines: list[str]) -> dict:
    result = []
    keys = lines[0].strip().split(' ')
    # keys.removeAt(0)

    current_values = lines[1].strip().split(' ')
    target_values = lines[2].strip().split(' ')
    validation_values = lines[3].strip().split(' ')

    result = {}

    key_map = {
        "수강학점": "grades",
        "취득학점": "major",
        "전공": "liberal_art",
        "교양": "grades",
        "졸업평점": "english",
        "영어강의": "paper",
        "논문": "division",
        "TOPIK": "foreign",
    }

    for idx, key in enumerate(keys):
        if key in ["구분", "판정", "판정 기준" ,""]: continue
            
        output = {}
        output["earned"] = target_values[idx + 1]
        output["required"] = current_values[idx + 1]
        output["valid"] = False if validation_values[idx+1] == "미통과" else True
        
        
        result[key_map[key]] = output
    
    return result

def parse_course_data(lines : list[str]) -> dict:
    parsed_rows = []
    current_category = None

    for line in lines:
        line = line.strip()
        if not line:
            continue

        #이수구분으로 시작하는 과목
        category_match = re.match(r'^(\d{2})\s+([A-Z]+\d+)\s+(.+?)\s+(\d)\s+(\d{4})\s*/\s*(\d)', line)
        if category_match:
            current_category = category_match.group(1) #이수구분 담는 거
            line = line[len(current_category):].strip()

        #분반 코드로 시작하는 과목
        match = re.match(r'^([A-Z]+\d+)\s+(.+?)\s+(\d)\s+(\d{4})\s*/\s*(\d)',line)
        if match and current_category:
            subject_code = match.group(1)
            subject_name = match.group(2)
            credit = match.group(3)  
            year = match.group(4)
            semester = match.group(5)
            
            if semester in ["14", "15", "16", "17"]:
                category = "liberal_art"
            else :
                category = "major"

            parsed_rows.append({
                "subject_code": current_category,
                "lecture_code": subject_code,
                "subject_name": subject_name,
                "enroll_year": year,
                "enroll_semester": semester,
                "creteria" : category
            })

    return parsed_rows
def parse_generation_education(lines: list[str]) -> list[dict]:
    result = []
    
    def parse_pass(text):
        return text.strip() != "미통과"

    raw = ' '.join(lines[1:]).strip()

    pattern = re.compile(
        r'(?P<name>[^\d\s/]+.*?)\s+'
        r'(?:(?P<area1>\d+)\s*/\s*(?P<area2>\d+)\s+)?'
        r'(?P<score1>\d+)\s*/\s*(?P<score2>\d+)\s+'
        r'(?P<pass>통과|미통과)'
    )

    for match in pattern.finditer(raw):
        name = match.group('name').strip()
        area = match.group('area1')
        if area is not None:
            area_str = f"{match.group('area1')}/{match.group('area2')}" #영역
        else:
            area_str = None

        score_str = f"{match.group('score1')}/{match.group('score2')}"
        pass_result = parse_pass(match.group('pass'))

        result.append({
            "category": name,
            # "영역(취득/기준)": area_str,
            "earned" : match.group('score1'),
            "required": match.group('score2'),
            "valid": pass_result
        })

    return result
def parse_major_education(lines: list[str]) -> dict:
    data_line = lines[-1]
    tokens = data_line.split()

    result = {
        "advanced_major": tokens[2],
        "reference_year": int(tokens[3]),
        "major_basic": {
            "earned": int(tokens[4]),
            "required": int(tokens[6]),
        },
        "major_required": {
            "earned": int(tokens[7]),
            "required": int(tokens[9]),
        },
        "major_required_plus_elective": {
            "earned": int(tokens[10]),
            "required": int(tokens[12]),
        },
        "valid": tokens[13] != "미통과"
    }

    return result
def parse_major_requirements(data: list) -> dict:
    result = {}

    for line in data:
        if "산학필수" in line:
            match = re.search(r"산학필수.*(\d+)\s*/\s*(\d+)", line)
            if match:
                result["major_industry_required"] = {
                    "earned_credit": int(match.group(1)),
                    "required_credit": int(match.group(2))
                }

        elif "전공필수" in line:
            match = re.search(r"전공필수.*(\d+)\s*/\s*(\d+)", line)
            if match:
                result["major_required_num"] = {
                    "earned_subject_num": int(match.group(1)),
                    "required_subject_num": int(match.group(2))
                }

    return result

def extract_major_requirement(data: str) -> list:
    result = [] 
    
    for idx, line in enumerate(data.splitlines()):
        print(line)
        if line in "졸업필수":
            result = data.splitlines()[idx:]
            break 
    
    return result  


def parse_pdf(pdf_path: str) -> dict:
    with pdfplumber.open(pdf_path) as pdf:
        page = pdf.pages[0]

        full_text = page.extract_text()
        result = parse_text_file(full_text.splitlines())

        if "교양/기타/금학기수강학점" not in full_text or "전공학점" not in full_text:
            raise ValueError("기준 텍스트가 페이지에 없습니다.")

        words = page.extract_words()
        split_x = None
        for word in words:
            if word['text'] == "전공학점":
                split_x = word['x0']
                break

        if split_x is None:
            raise ValueError("기준 텍스트 '전공학점'의 위치를 찾을 수 없습니다.")

        width = page.width
        height = page.height

        left_crop = page.crop((0, 250, split_x, height))
        left_text = left_crop.extract_text()

        right_crop = page.crop((split_x, 250, width, height))
        right_text = right_crop.extract_text()
        
        result[Section.COURSE_DETAILS] = right_text.splitlines() + left_text.splitlines()
        result[Section.MAJOR_REQUIREMENT] = extract_major_requirement(right_text)
    
    basic_info_dict = parse_student_info(result[Section.BASIC_INFO])
    graduation_info_dict = parse_graduation_data(result[Section.GRADUATION_INFO])
    course_detail_info_dict = parse_course_data(result[Section.COURSE_DETAILS])
    liberal_arts_list = parse_generation_education(result[Section.GENERAL_EDUCATION])
    major_dict = parse_major_education(result[Section.MAJOR_EDUCATION])
    major_dict.update(parse_major_requirements(result[Section.MAJOR_REQUIREMENT]))

    merged_dict = {
        "basic_info": basic_info_dict,
        "graduation_info": graduation_info_dict,
        "liberal_arts_info" : liberal_arts_list,
        "major_info" : major_dict,
        "course_info" : course_detail_info_dict,
    }

    return merged_dict
