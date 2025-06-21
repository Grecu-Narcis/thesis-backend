from PIL import Image

import torch
import torchvision
from scipy.io import loadmat
from torch import nn
from ultralytics import YOLO

from Converter import Converter
from label_manager import load_or_init_class_names


class Classifier:
    def __init__(self):
        self.converter = Converter()
        cars_meta_mat = loadmat("./cars_meta.mat")
        self.class_names = load_or_init_class_names()
        self.class_names = [str(arr[0]) for arr in cars_meta_mat['class_names'][0]]

        print(self.class_names)

        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

        self.model = torchvision.models.efficientnet_b4().to(self.device)
        self.model.classifier[1] = nn.Linear(in_features=self.model.classifier[1].in_features,
                                             out_features=len(self.class_names)).to(self.device)
        self.model.load_state_dict(torch.load("./efficientnet_b4-accuracy-87.pth", map_location=torch.device("cpu")))
        self.model.eval()

        # Load car detection model
        self.detection_model = YOLO('yolo11m.pt').to(self.device)
        self.detection_model.eval()

        self.model_transforms = torchvision.models.EfficientNet_B4_Weights.DEFAULT.transforms()

    def detect_car(self, image: Image.Image) -> tuple[bool, tuple[int, int, int, int] | None]:
        image_resized = image.resize((640, 640))  # Match YOLO input
        image_tensor = torchvision.transforms.ToTensor()(image_resized).unsqueeze(0).to(self.device)

        with torch.inference_mode():
            predictions = self.detection_model(image_tensor)

            for box in predictions[0].boxes:
                if box.conf.item() > 0.5 and int(box.cls.item()) == 2:  # class 2 = 'car'
                    x1, y1, x2, y2 = map(int, box.xyxy[0].tolist())

                    # Scale box back to original image size
                    w_ratio = image.width / 640
                    h_ratio = image.height / 640
                    scaled_box = (
                        int(x1 * w_ratio),
                        int(y1 * h_ratio),
                        int(x2 * w_ratio),
                        int(y2 * h_ratio)
                    )

                    return True, scaled_box

        return False, None

    def make_prediction(self, image: Image.Image):
        try:
            with torch.inference_mode():
                transformed_image = self.model_transforms(image).unsqueeze(0).to(self.device)
                output = self.model(transformed_image)
                pred_label = torch.argmax(torch.softmax(output, dim=1), dim=1).item()
                return self.converter.convert_to_brand_model(self.class_names[pred_label - 1])

        except Exception as e:
            print(f"Prediction error: {e}")
            return "Unknown"

    def detect_and_classify(self, image: Image.Image) -> str:
        detected, box = self.detect_car(image)

        if not detected or box is None:
            return "No car detected"

        cropped = image.crop(box)
        return self.make_prediction(cropped)

    def get_all_brands(self):
        return self.converter.get_all_brands()

    def get_all_models(self, brand: str):
        return self.converter.get_all_models(brand)

    def reload(self, model_path: str):
        print(f"🔁 Reloading model from: {model_path}")
        self.class_names = load_or_init_class_names()

        self.model = torchvision.models.efficientnet_b4().to(self.device)
        self.model.classifier[1] = nn.Linear(
            in_features=self.model.classifier[1].in_features,
            out_features=len(self.class_names)
        ).to(self.device)

        self.model.load_state_dict(torch.load(model_path, map_location=self.device))
        self.model.eval()
        print("✅ Reload complete.")

