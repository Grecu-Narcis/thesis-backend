# retrain.py
import os
from datetime import datetime

import torch
import torch.nn as nn
import torch.optim as optim
import torchvision
from torch.utils.data import DataLoader
import torchvision.transforms as transforms
import torchvision.models as models
from PIL import Image
from pathlib import Path
import pandas as pd
import mysql.connector
import requests
import json

# --- CONFIG ---

RELOAD_API_URL = os.getenv("RELOAD_API_URL", "http://localhost:5000/reload-model")

IMAGE_DIR = Path("images")
LABEL_FILE = Path("label_registry.json")
MODEL_DIR = Path("model")


class CarDataset(torch.utils.data.Dataset):
    def __init__(self, df, image_dir, label_to_idx, transform):
        self.df = df
        self.image_dir = image_dir
        self.label_to_idx = label_to_idx
        self.transform = transform

    def __len__(self):
        return len(self.df)

    def __getitem__(self, idx):
        row = self.df.iloc[idx]
        image_path = self.image_dir / row['imageKey'].replace("/", "_")
        image = Image.open(image_path).convert("RGB")
        if self.transform:
            image = self.transform(image)
        label = f"{row['carBrand']} {row['carModel']} {row['productionYear']}"
        return image, self.label_to_idx[label]


def fetch_data_from_db():
    print("Connecting to DB...")
    conn = mysql.connector.connect(**DB_CONFIG)
    df = pd.read_sql("SELECT DISTINCT imageKey, carBrand, carModel, productionYear FROM posts WHERE imageKey IS NOT NULL", conn)
    conn.close()
    return df


def download_images(df):
    IMAGE_DIR.mkdir(exist_ok=True)
    print("Downloading images...")
    for _, row in df.iterrows():
        image_key = row['imageKey']
        local_name = image_key.replace("/", "_")
        local_path = IMAGE_DIR / local_name
        if local_path.exists():
            continue
        url = f"{CLOUDFRONT_URL}/{image_key}"
        try:
            r = requests.get(url, timeout=10)
            if r.ok:
                with open(local_path, "wb") as f:
                    f.write(r.content)
        except Exception as e:
            print(f"Failed to download {image_key}: {e}")


def prepare_labels(df):
    print("Preparing labels...")
    labels = sorted(set(f"{row['carBrand']} {row['carModel']} {row['productionYear']}" for _, row in df.iterrows()))
    with open(LABEL_FILE, "w") as f:
        json.dump(labels, f, indent=2)
    label_to_idx = {label: idx for idx, label in enumerate(labels)}
    return labels, label_to_idx


def create_dataloader(df, label_to_idx):
    transform = transforms.Compose([
        transforms.Resize((380, 380)),
        transforms.ToTensor(),
        transforms.Normalize([0.5]*3, [0.5]*3)
    ])
    dataset = CarDataset(df, IMAGE_DIR, label_to_idx, transform)
    return DataLoader(dataset, batch_size=16, shuffle=True, num_workers=2)


def train_model(dataloader, num_classes):
    print("Training model...")
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    effnet_weights = torchvision.models.EfficientNet_B4_Weights.DEFAULT
    model = models.efficientnet_b4(weights=effnet_weights)
    model.classifier[1] = nn.Linear(model.classifier[1].in_features, num_classes)
    model = model.to(device)

    optimizer = optim.Adam(model.parameters(), lr=1e-4, weight_decay=5e-4)
    criterion = nn.CrossEntropyLoss()

    for epoch in range(3):
        model.train()
        running_loss = 0.0
        for images, targets in dataloader:
            images, targets = images.to(device), targets.to(device)
            optimizer.zero_grad()
            outputs = model(images)
            loss = criterion(outputs, targets)
            loss.backward()
            optimizer.step()
            running_loss += loss.item()
        print(f"Epoch {epoch+1} loss: {running_loss:.4f}")

    return model


def save_model(model):
    MODEL_DIR.mkdir(exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    model_path = MODEL_DIR / f"efficientnet_b4_{timestamp}.pt"
    torch.save(model.state_dict(), model_path)
    print(f"Model saved to {model_path}")
    return model_path


def cleanup_images():
    if IMAGE_DIR.exists():
        print("Cleaning up downloaded images...")
        for file in IMAGE_DIR.glob("*"):
            try:
                file.unlink()
            except Exception as e:
                print(f"Failed to delete {file}: {e}")


def notify_reload(model_path):
    try:
        response = requests.post(RELOAD_API_URL, json={"path": str(model_path)})
        if response.ok:
            print("✅ Model reload triggered successfully.")
        else:
            print(f"⚠️ Failed to trigger reload. Status code: {response.status_code}")
    except Exception as e:
        print(f"❌ Error triggering reload: {e}")


def retrain_model():
    df = fetch_data_from_db()
    download_images(df)
    labels, label_to_idx = prepare_labels(df)
    dataloader = create_dataloader(df, label_to_idx)
    model = train_model(dataloader, len(labels))
    path = save_model(model)
    cleanup_images()
    
    notify_reload(path)


if __name__ == "__main__":
    retrain_model()
