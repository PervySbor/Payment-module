package payment.module.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import payment.module.services.PollService;
import payment.module.util.JsonManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class PollServlet extends HttpServlet {
    private PollService ps;

    @Override
    public void init(){
        this.ps = (PollService) this.getServletContext().getAttribute("pollService");
    }

    //http:localhost:8080/myApp/payment/poll?txHash=3458390504953048538945
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String txHash = request.getParameter("txHash");

        Map<String, String> data = this.ps.checkPayment(txHash);

        if(!request.getHeader("Content-Type").equals("application/json")){
            returnError(response,422, "Incorrect content type");
        }

        PrintWriter writer = response.getWriter();
        writer.write(data.get("json"));
        response.setStatus(Integer.parseInt(data.get("statusCode")));

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
