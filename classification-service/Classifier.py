from PIL import Image

import torch
import torchvision
from scipy.io import loadmat
from torch import nn
from ultralytics import YOLO

from Converter import Converter


class Classifier:
    def __init__(self):
        self.converter = Converter()
        cars_meta_mat = loadmat("./cars_meta.mat")
        self.class_names = [str(arr[0]) for arr in cars_meta_mat['class_names'][0]]

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


    def detect_car(self, image: Image.Image) -> bool:
        transform = torchvision.transforms.Compose([
            torchvision.transforms.Resize((640, 640)),
            torchvision.transforms.ToTensor()
        ])

        image_tensor = transform(image).unsqueeze(0).to(self.device)

        with torch.inference_mode():
            predictions = self.detection_model(image_tensor)

            for box in predictions[0].boxes:
                print(box.conf.item(), box.cls.item())
                if box.conf.item() > 0.5 and int(box.cls.item()) == 2:  # COCO class 2 corresponds to 'car'
                    return True

        return False

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


    def get_all_brands(self):
        return self.converter.get_all_brands()

    def get_all_models(self, brand: str):
        return self.converter.get_all_models(brand)
