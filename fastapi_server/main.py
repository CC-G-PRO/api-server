from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from pdf_parser import parse_pdf
import io
app = FastAPI()

@app.post("/parse-pdf/")
async def parse_pdf_endpoint(pdf_file: UploadFile = File(...)):
    try:
        pdf_content = await pdf_file.read()

        pdf_path = "/tmp/temp.pdf"
        with open(pdf_path, "wb") as f:
            f.write(pdf_content)

        parsed_data = parse_pdf(pdf_path)

        return JSONResponse(content=parsed_data)
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
