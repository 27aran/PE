package de.unistuttgart.iste.ese.api;

import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.jpmml.evaluator.OutputField;
import org.jpmml.evaluator.TargetField;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A class for loading and using a PMML-based Todo classification model.
 */
public class TodoModel {
    private static final Logger LOG = LoggerFactory.getLogger(TodoModel.class);
    private Evaluator evaluator;

    /**
     * Constructs a TodoModel with the specified PMML model file path.
     *
     * @param pathname The path to the PMML model file.
     */
    public TodoModel(String pathname) {
        loadModel();
    }

    /**
     * Loads the PMML model from the specified file path and initializes the model
     * evaluator.
     * If the loading process encounters any exceptions, the evaluator is set to
     * null.
     */
    public void loadModel() {
        // Building a model evaluator from a PMML file
        try {
            this.evaluator = new LoadingModelEvaluatorBuilder()
                .load(getClass().getClassLoader().getResourceAsStream("model.pmml"))
                .build();
        } catch (ParserConfigurationException | SAXException | JAXBException e) {
            LOG.error("Could not load AI model:", e);
            this.evaluator = null;
        }
    }

    /**
     * Parses the model's output to retrieve the predicted class/category.
     *
     * @param results The results obtained from evaluating the model on input data.
     * @return The predicted class/category based on the model's output.
     */
    private String parseModelOutput(Map<String, ?> results) {

        for (Object value : results.values()) {

            if (value instanceof org.jpmml.evaluator.ProbabilityDistribution pd) {

                int index = ((Number) pd.getResult()).intValue();

                TargetField targetField = evaluator.getTargetFields().get(0);
                List<?> values = targetField.getField().getValues();

                if (index >= 0 && index < values.size()) {
                    return values.get(index).toString();
                }
            }
        }

        LOG.warn("No ProbabilityDistribution found, returning GENERAL");
        return "GENERAL";
    }

    /**
     * Predicts the class/category of a given input text.
     *
     * @param inputString The input text to be classified.
     * @return The predicted class/category for the input text.
     */
    public String predictClass(String inputText) {

        if (evaluator == null) {
            LOG.warn("Model not loaded, returning GENERAL");
            return "GENERAL";
        }

        Map<String, Object> input = new HashMap<>();
        input.put("text", inputText);

        Map<String, ?> results = evaluator.evaluate(input);

        // 🔍 DEBUG – DAS IST WICHTIG
        LOG.info("PMML RESULTS: " + results);

        // 1️⃣ Hole TargetField
        TargetField targetField = evaluator.getTargetFields().get(0);

        Object targetValue = results.get(targetField.getName());

        // 2️⃣ ProbabilityDistribution → bestes Label holen
        if (targetValue instanceof org.jpmml.evaluator.ProbabilityDistribution<?> pd) {
            Object best = pd.getResult();
            return mapIndexToLabel(best);
        }

        // 3️⃣ Direkter Wert
        return mapIndexToLabel(targetValue);
    }


    private String mapIndexToLabel(Object value) {

        if (value == null) {
            return "GENERAL";
        }

        int index;
        try {
            index = Integer.parseInt(value.toString());
        } catch (Exception e) {
            return value.toString().toUpperCase();
        }

        return switch (index) {
            case 0 -> "private";
            case 1 -> "work";
            default -> "general";
        };
    }

    /**
     * Unloads the loaded PMML model, releasing resources.
     */
    public void unloadModel() {
        this.evaluator = null;
    }
}
