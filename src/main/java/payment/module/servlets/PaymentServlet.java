package payment.module.servlets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import payment.module.services.ValidationService;
import payment.module.util.JsonManager;
import payment.module.util.LogManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PaymentServlet extends HttpServlet {
    private ValidationService vService;

    @Override
    public void init(){
        this.vService = (ValidationService) this.getServletContext().getAttribute("validationService");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response){
        try {
            if(!request.getHeader("Content-Type").equals("application/json")){
                returnError(response,422, "Incorrect content type");
            }
            String jsonBody = request.getReader().readLine(); //as the whole json must be on a single line, according to the HTTP/1.1
            Map<String,String> data = this.vService.validate(jsonBody);

            response.setHeader("Content-Type","application/json");
            response.setStatus(Integer.parseInt(data.get("statusCode")));

            PrintWriter writer = response.getWriter();
            writer.write(data.get("json"));

        } catch (IOException e) {
            LogManager.logException(e, Level.SEVERE);
            returnError(response, 500, "Encountered error in PaymentServlet doPost()");
        }
    }

    private void returnError(HttpServletResponse response, int statusCode, String message){
        response.setHeader("Content-Type","application/json");
        try {
            PrintWriter writer = response.getWriter();
            Map<String, String> toBeSerialized = new HashMap<String,String>();
            toBeSerialized.put("message", message);
            String serializedError = JsonManager.serialize(toBeSerialized);
            writer.write(serializedError);
            response.setStatus(statusCode);
        } catch(IOException e){
            response.setStatus(500);
        }
    }
}
