package payment.module.validation;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import payment.module.util.LogManager;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            String jsonBody = request.getReader().readLine(); //as the whole json must be on a single line, according to the HTTP/1.1
            this.vService.validate(jsonBody);
        } catch (IOException e) {
            LogManager.logException(e, Level.SEVERE);
        }
    }
}
