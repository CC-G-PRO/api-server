FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt /app/
COPY main.py /app/
COPY pdf_parser.py /app/

RUN pip install --no-cache-dir -r requirements.txt
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8080"]
