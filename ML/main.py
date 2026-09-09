from fastapi import FastAPI

app = FastAPI(title="SchemeGuard ML Service")


@app.get("/health")
def health() -> dict[str, str]:
    return {
        "status": "ok",
        "service": "ml"
    }


@app.post("/predict")
def predict(payload: dict) -> dict:
    return {
        "status": "not_implemented",
        "message": "ML prediction placeholder",
        "input": payload
    }