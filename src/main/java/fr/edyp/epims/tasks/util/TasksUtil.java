package fr.edyp.epims.tasks.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.edyp.epims.dataaccess.TaskError;
import fr.edyp.epims.util.error.ErrorResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class TasksUtil {

  /**
   * Tests the status code of a given response and attempts to handle any errors by
   * analyzing the response or performing an additional request for error details.
   *
   * @param response the original response to be validated
   * @param restTemplate the RestTemplate instance used for performing subsequent requests
   * @param requestEntity the HTTP entity containing request headers and body for the additional request
   * @param requestURL the URL used for the additional request to fetch error details
   * @return a TaskError object representing the error details if the status is not success
   *         or null if no error exists
   */
  public static TaskError testStatusCode(ResponseEntity<?> response, RestTemplate restTemplate, HttpEntity<?> requestEntity, String requestURL) {

    HttpStatusCode statusCode = response.getStatusCode();
    TaskError taskError = null;
    if (!statusCode.is2xxSuccessful()) {
      //Error calling task. Try to get more information
      try {
        ResponseEntity<ErrorResponse> errorResponse = restTemplate.exchange(requestURL, HttpMethod.POST, requestEntity, ErrorResponse.class);
        ErrorResponse error = errorResponse.getBody();
        if (error != null) {
          taskError = new TaskError("Error " + error.getErrorCode(),
                  error.getMessage() + (error.getDetails() != null ? " - " + error.getDetails() : ""));
        } else {
          taskError = new TaskError("Failed for unknown reason");
        }
      } catch (Exception e) {
        taskError = new TaskError("Failed for unknown reason");
      }
    }

    return taskError;
  }

  public static TaskError fromStatusCodeException(HttpStatusCodeException sce) {
    TaskError taskError = null;
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      ErrorResponse error = mapper.readValue(sce.getResponseBodyAsString(), ErrorResponse.class);
      taskError = new TaskError("Error " + error.getErrorCode(), error.getMessage());
    } catch (Exception parseException) {
      parseException.printStackTrace();
      taskError = new TaskError("HTTP " + sce.getStatusCode(), sce.getMessage());
    }
    return taskError;
  }
}
