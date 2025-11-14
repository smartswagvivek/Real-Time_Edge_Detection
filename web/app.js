"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
const fileInput = document.getElementById("fileInput");
const uploadedImage = document.getElementById("uploadedImage");
const canvas = document.getElementById("canvasOutput");
const loader = document.getElementById("loader");
const ctx = canvas.getContext("2d");
// Handle PNG upload
fileInput.addEventListener("change", () => __awaiter(void 0, void 0, void 0, function* () {
    var _a;
    const file = (_a = fileInput.files) === null || _a === void 0 ? void 0 : _a[0];
    if (!file)
        return;
    loader.style.display = "block";
    const url = URL.createObjectURL(file);
    uploadedImage.src = url;
    uploadedImage.onload = () => {
        canvas.width = uploadedImage.width;
        canvas.height = uploadedImage.height;
        ctx.drawImage(uploadedImage, 0, 0);
        loader.style.display = "none";
    };
}));
