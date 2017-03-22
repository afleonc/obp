package controllers;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import models.SolicitudHistoriaObp;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    public static Result getSolicitudHistoriaLaboral(String user, String password, String tipoId, String numeroId) {
        ConsultarHistoriaLaboral consultaHistoriaLaboral = new ConsultarHistoriaLaboral();
        HtmlPage paginaPrincipal = (HtmlPage) Cache.get("paginaPrincipal");
        WebClient webClient = (WebClient) Cache.get("webClient");
        String resultadoConsulta = consultaHistoriaLaboral.consultarHistoriaLaboral(user, password, tipoId, numeroId, paginaPrincipal, webClient);

        if (resultadoConsulta.equals("EXECUTION_EXCEPTION") || resultadoConsulta.equals("NO_PAGE") || resultadoConsulta.equals("NO_RESULT")) {
            Cache.remove("paginaPrincipal");
            Cache.remove("webClient");
            resultadoConsulta = consultaHistoriaLaboral.consultarHistoriaLaboral(user, password, tipoId, numeroId, null, null);
            if (resultadoConsulta.equals("EXECUTION_EXCEPTION") || resultadoConsulta.equals("NO_PAGE") || resultadoConsulta.equals("NO_RESULT")) {
                Cache.remove("paginaPrincipal");
                Cache.remove("webClient");
                SolicitudHistoriaObp solicitudHistoriaObp = new SolicitudHistoriaObp();
                solicitudHistoriaObp.setTipoId(tipoId);
                solicitudHistoriaObp.setNumeroId(numeroId);
                solicitudHistoriaObp.setSolicitudObtenida(false);
                Gson serializer = new Gson();
                return ok(serializer.toJson(solicitudHistoriaObp));
            } else {
                Cache.set("paginaPrincipal", consultaHistoriaLaboral.getPaginaPrincipal());
                Cache.set("webClient", consultaHistoriaLaboral.getWebClient());
                return ok(resultadoConsulta);
            }
        } else {
            Cache.set("paginaPrincipal", consultaHistoriaLaboral.getPaginaPrincipal());
            Cache.set("webClient", consultaHistoriaLaboral.getWebClient());
            return ok(resultadoConsulta);
        }
    }

}
