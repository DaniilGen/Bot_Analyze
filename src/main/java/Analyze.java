import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;

public class Analyze {
    public List<String> search(String path) throws IOException {

        ByteString imageBytes = ByteString.readFrom(new FileInputStream(path));
        Image image = Image.newBuilder().setContent(imageBytes).build();

        Feature feature = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);

        ImageAnnotatorClient client = ImageAnnotatorClient.create();
        BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
        List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
        AnnotateImageResponse imageResponse = imageResponses.get(0);

        if (imageResponse.hasError()) {
            System.out.println("Error: " + imageResponse.getError().getMessage());
        }

        List<String> info = new ArrayList<>();
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        for (EntityAnnotation annotation : imageResponse.getLabelAnnotationsList()) {
            Translation translation = translate.translate(annotation.getDescription(),
                    TranslateOption.sourceLanguage("en"),
                    TranslateOption.targetLanguage("ru"));
            String result = String.format("%.2f", annotation.getScore() * 100);
            info.add(translation.getTranslatedText() + ": " + (result) + " %");
        }

        client.close();

        feature = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
        request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
        requests = new ArrayList<>();
        requests.add(request);

        client = ImageAnnotatorClient.create();
        batchResponse = client.batchAnnotateImages(requests);
        imageResponses = batchResponse.getResponsesList();
        imageResponse = imageResponses.get(0);

        if (imageResponse.hasError()) {
            System.out.println("Error: " + imageResponse.getError().getMessage());
        }
        info.add("\n ❗️А теперь поищем текст \uD83D\uDC47 \n");
        StringBuilder text = new StringBuilder();
        int key = 0;
        for (EntityAnnotation annotation : imageResponse.getTextAnnotationsList()) {
            if (key == 0) {
                key = 1;
            } else {
                text.append(annotation.getDescription()).append(" ");
            }

        }
        if (text.isEmpty()) {
            text.append("Кажется текста Я не нашел \uD83D\uDE22");
        }
        info.add(String.valueOf(text));
        if (!text.isEmpty()) {
            Detection detection = translate.detect(String.valueOf(text));
            String detectedLanguage = detection.getLanguage();
            if (!detectedLanguage.equals("ru")) {
                try {
                    Translation translation = translate.translate(
                            String.valueOf(text),
                            TranslateOption.sourceLanguage(detectedLanguage),
                            TranslateOption.targetLanguage("ru"));
                    info.add("\n ❗️Ну и перевод на всякий случай \uD83D\uDD25 \n");
                    info.add(translation.getTranslatedText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        client.close();

        feature = Feature.newBuilder().setType(Type.LANDMARK_DETECTION).build();
        request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
        requests = new ArrayList<>();
        requests.add(request);

        client = ImageAnnotatorClient.create();
        batchResponse = client.batchAnnotateImages(requests);
        imageResponses = batchResponse.getResponsesList();
        imageResponse = imageResponses.get(0);

        if (imageResponse.hasError()) {
            System.out.println("Error: " + imageResponse.getError().getMessage());
        }

        text = new StringBuilder();
        key = 0;
        for (EntityAnnotation annotation : imageResponse.getLandmarkAnnotationsList()) {
            if (key == 0) {
                key = 1;
            } else {
                try {
                    Translation translation = translate.translate(annotation.getDescription(),
                            TranslateOption.sourceLanguage("en"),
                            TranslateOption.targetLanguage("ru"));
                    text.append(translation.getTranslatedText()).append("\n");
                } catch (Exception e) {
                    text.append(annotation.getDescription()).append("\n");
                }

            }
        }
        if (!text.isEmpty()) {
            info.add("\n ❗️Я смог найти достопримечательность! \n");
            info.add(String.valueOf(text));
        }

        client.close();

        feature = Feature.newBuilder().setType(Type.LOGO_DETECTION).build();
        request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
        requests = new ArrayList<>();
        requests.add(request);

        client = ImageAnnotatorClient.create();
        batchResponse = client.batchAnnotateImages(requests);
        imageResponses = batchResponse.getResponsesList();
        imageResponse = imageResponses.get(0);

        if (imageResponse.hasError()) {
            System.out.println("Error: " + imageResponse.getError().getMessage());
        }

        text = new StringBuilder();
        for (EntityAnnotation annotation : imageResponse.getLogoAnnotationsList()) {
            text.append(annotation.getDescription()).append("\n");
        }
        if (!text.isEmpty()) {
            info.add("\n ❗️Я смог найти логотип \uD83D\uDCA5 \n");
            info.add(String.valueOf(text));
        }

        client.close();
        return info;
    }
}
