# Admin Platform – CSV Upload for Chatbot Configuration

A web‑based admin interface for uploading and validating CSV files that configure the chatbot's Q&A knowledge base.

## Quick Start

```bash
# From the scouting-ai-service directory
pip install -r requirements.txt
uvicorn adminplatform.main:app --port 8000 --reload
```

Then open [http://localhost:8000/admin/upload](http://localhost:8000/admin/upload).

---

## CSV Schema

The uploaded CSV must use **semicolons (`;`)** or **commas (`,`)** as delimiters — both formats are auto‑detected.

| Column Name  | Required | Description                                  | Example                                      |
|-------------|----------|----------------------------------------------|----------------------------------------------|
| `Question`  | ✅ Yes   | The user question or utterance               | What are the training hours?                 |
| `Answer`    | ✅ Yes   | The chatbot's response                       | Training is Mon–Fri, 16:00–18:00.            |
| `Category`  | ❌ No    | Topic category for routing / analytics       | Training/Schedule                            |
| `Source`    | ❌ No    | Source document reference                    | Academy Info                                 |

### Example CSV (semicolon‑delimited)

```csv
Question;Answer;Category;Source
What are the fees?;Annual fee is 1200 TND payable in instalments.;Fee Info;Academy Info
How to register?;Visit our website or come to the office Mon-Sat 09:00-18:00.;Registration;Academy Info
Do you have U15 teams?;Yes, we have U13, U15, U17 and U19 categories.;Teams;Coaches Handbook
```

### Example CSV (comma‑delimited)

```csv
Question,Answer,Category,Source
What are the fees?,Annual fee is 1200 TND payable in instalments.,Fee Info,Academy Info
How to register?,"Visit our website or come to the office Mon-Sat 09:00-18:00.",Registration,Academy Info
```

---

## Upload Behaviour

1. The admin selects or drag‑and‑drops a `.csv` file.
2. The server validates:
   - File extension is `.csv`.
   - The file is valid UTF‑8 text.
   - Required columns (`Question`, `Answer`) are present.
3. If valid, the file is:
   - Stored in `data/chatbot_uploads/` with a timestamp prefix.
   - **Copied** to the chatbot's training data directory (`chatbot/chatbot/apps/chat/training_models/data/data.csv`) so the chatbot picks it up on next restart.
4. A preview of the first 5 rows is displayed on screen.

---

## API Endpoints

| Method | Path             | Description                        |
|--------|------------------|------------------------------------|
| GET    | `/admin/upload`  | Render the upload form             |
| POST   | `/admin/upload`  | Validate & store the uploaded CSV  |
| GET    | `/`              | Redirect to `/admin/upload`        |
