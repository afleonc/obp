/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.SolicitudHistoriaObp;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author afleo
 */
class ConsultarHistoriaLaboral {

    private  WebClient webClient = new WebClient(BrowserVersion.CHROME);
    private  HtmlPage paginaPrincipal;
    private  String tipoId;
    private  String numeroId;

    public  WebClient getWebClient() {
        return webClient;
    }

    public  HtmlPage getPaginaPrincipal() {
        return paginaPrincipal;
    }

    String consultarHistoriaLaboral(String user, String password, String tipoId, String numeroId, HtmlPage loginPage, WebClient webClient) {
        long tiempoInicial = new Date().getTime();
        this.tipoId = tipoId;
        this.numeroId = numeroId;
        try {
            if (webClient == null) {
                webClientFactory();
                System.out.println("No encontro WebClient se ha creado uno nuevo");
            }
            paginaPrincipal = loginPage;
            if (paginaPrincipal == null) {
                paginaPrincipal = login(user, password);
                System.out.println("No se encontro el login se loguea nuevamente");
            }
            if (paginaPrincipal != null) {
                HtmlPage historicoBonos = consultarBonosAfiliado(tipoId, numeroId, paginaPrincipal);
                if (historicoBonos != null) {
                    String historicoBonosPensionales = obtenerHistoricoBonosPensionales(historicoBonos);
                    System.out.println(historicoBonosPensionales);
                    return crearHistoricoBonos(historicoBonosPensionales);
                } else {
                    cerrarSesion(paginaPrincipal);
                    return "NO_RESULT";
                }
            }
            return "NO_PAGE";
        } catch (Exception e){
            System.out.println("No se pudo obtener la página principal");
            return "EXECUTION_EXCEPTION";
        } finally {
            long tiempoFinal = new Date().getTime();
            System.out.println("Tiempo total " + (tiempoFinal - tiempoInicial) + "ms");
        }

    }

    private void webClientFactory() {
        long tiempoInicial = new Date().getTime();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getCookieManager().setCookiesEnabled(true);
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        long tiempoFinal = new Date().getTime();
        System.out.println("Tiempo de web factory " + (tiempoFinal - tiempoInicial) + "ms");

    }

    private HtmlPage login(String username, String password) {
        long tiempoInicial = new Date().getTime();
        HtmlPage loginPage;
        try {
            loginPage = webClient.getPage("https://www.bonospensionales.gov.co/BonosPensionales");
            System.out.println("Pagina obtenida");
            HtmlForm loginForm = loginPage.getFormByName("F");
            HtmlTextInput usernameElement = loginForm.getInputByName("ssousername");
            HtmlPasswordInput passwordElement = loginForm.getInputByName("password");
            HtmlSubmitInput loginButton = (HtmlSubmitInput) loginPage.querySelectorAll(".BOT").get(0);
            usernameElement.setValueAttribute(username);
            passwordElement.setValueAttribute(password);
            return loginButton.click();
        } catch(UnknownHostException ex) {
            Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, "Falla en la conexión a internet" );
            Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, "Error en la dirección de la pagina Web");
            Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (FailingHttpStatusCodeException ex) {
            Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, "Error al obtener la pagina");
            Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally{
            long tiempoFinal = new Date().getTime();
            System.out.println("Tiempo de login " + (tiempoFinal - tiempoInicial) + "ms");
        }

    }

    private static HtmlPage consultarBonosAfiliado(String tipoDocumento, String numeroDocumento, HtmlPage paginaPrincipal) {
        long tiempoInicial = new Date().getTime();
        try {
            HtmlPage paginaBusqueda = paginaPrincipal.getElementById("SUBSOLICITUDES").getElementsByTagName("td").get(0).click();
            if (paginaBusqueda != null) {
                String tipoIdBonos;
                switch (tipoDocumento) {
                    case "CC":
                        tipoIdBonos = "C";
                        break;
                    case "CE":
                        tipoIdBonos = "E";
                        break;
                    case "TI":
                        tipoIdBonos = "T";
                        break;
                    default:
                        tipoIdBonos = "";
                        break;
                }

                try {
                    HtmlForm consultaBonos = paginaBusqueda.getFormByName("F");
                    HtmlSelect tipoDocumentoElement = consultaBonos.getSelectByName("tipoDocumento");
                    HtmlOption opcionTipoDocumento = tipoDocumentoElement.getOptionByValue(tipoIdBonos);
                    tipoDocumentoElement.setSelectedAttribute(opcionTipoDocumento, true);
                    HtmlTextInput numeroDocumentoElement = consultaBonos.getInputByName("documento");
                    numeroDocumentoElement.setText(numeroDocumento);
                    HtmlSubmitInput botonConsultar = consultaBonos.getInputByName("cons");
                    return botonConsultar.click();
                } catch (NullPointerException ex) {
                    System.out.println("No se ha encontrado el elemento especifico en la página");
                    Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }
        } catch (IOException ex) {
            System.out.println("No se ha logrado cargar la página");
            Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            long tiempoFinal = new Date().getTime();
            System.out.println("Tiempo de diligenciamiento de busqueda " + (tiempoFinal - tiempoInicial) + "ms");
        }
        return null;
    }

    private static String obtenerHistoricoBonosPensionales(HtmlPage historicoBonos) {
        long tiempoInicial = new Date().getTime();
        Document doc = Jsoup.parse(historicoBonos.asXml());
        String[] cabecera = {"Documento", "Nombres", "EstadoEmisor", "Observaciones"};
        Elements elements = doc.select(".TABLARESULTADOS tr.FOS");
        String json = "{\"HistoriaSolicitudes\":[";
        for (Element row : elements) {
            String jsonObject = "{";
            Elements historiaSolicitudes = row.getElementsByTag("td");
            jsonObject += "\"" + cabecera[0] + "\" : \"" + historiaSolicitudes.get(0).text() + "\",";
            jsonObject += "\"" + cabecera[1] + "\" : \"" + historiaSolicitudes.get(7).text() + "\",";
            jsonObject += "\"" + cabecera[2] + "\" : \"" + historiaSolicitudes.get(12).text() + "\",";
            jsonObject += "\"" + cabecera[3] + "\" : \"" + historiaSolicitudes.get(14).text() + "\",";
            if (jsonObject.length() != 1) {
                jsonObject = jsonObject.substring(0, jsonObject.length() - 1);
            }
            jsonObject += "},";
            json += jsonObject;
        }
        if (json.length() != 24) {
            json = json.substring(0, json.length() - 1);
        }
        json += "]}";
        long tiempoFinal = new Date().getTime();
        System.out.println("Tiempo de análisis de datos " + (tiempoFinal - tiempoInicial) + "ms");
        return json;
    }

    private String crearHistoricoBonos(String historicoBonosJson) {
        long tiempoInicial = new Date().getTime();

        JsonObject jsonObjectHistoricoBonos = new JsonParser().parse(historicoBonosJson).getAsJsonObject();
        JsonArray historico = jsonObjectHistoricoBonos.getAsJsonArray("HistoriaSolicitudes");
        if (historico.size() >= 1) {
            SolicitudHistoriaObp solicitudHistoriaObp = new SolicitudHistoriaObp();
            try {
                String tipoNumeroDocumento = historico.get(0).getAsJsonObject().get("Documento").getAsString();
                String tipoDocumentoAfiliado;
                switch (tipoNumeroDocumento.substring(0, 1)) {
                    case "C":
                        tipoDocumentoAfiliado = "CC";
                        break;
                    case "E":
                        tipoDocumentoAfiliado = "CE";
                        break;
                    case "T":
                        tipoDocumentoAfiliado = "TI";
                        break;
                    default:
                        tipoDocumentoAfiliado = null;
                        break;
                }
                solicitudHistoriaObp.setTipoId(tipoDocumentoAfiliado);
                solicitudHistoriaObp.setNumeroId(historico.get(0).getAsJsonObject().get("Documento").getAsString().substring(2));
                Set<Integer> mensajesObp = new HashSet<>();
                Set<String> estadosEmisor = new HashSet<>();

                for (int i = 0; i < historico.size(); i++) {
                    estadosEmisor.add(historico.get(i).getAsJsonObject().get("EstadoEmisor").getAsString());
                    String[] mensajesRegistro = historico.get(i).getAsJsonObject().get("Observaciones").getAsString().split("[:,]");
                    for (String aMensajesRegistro : mensajesRegistro) {
                        try {
                            Integer mensajeObp = Integer.parseInt(aMensajesRegistro);
                            mensajesObp.add(mensajeObp);
                        } catch (NumberFormatException ex) {
                            System.out.println("No se pudo obtener el valor numerico de " + aMensajesRegistro);
                        }
                    }
                }
                solicitudHistoriaObp.setEstadosEmisor(estadosEmisor);
                solicitudHistoriaObp.setMensajesOBP(mensajesObp);
                solicitudHistoriaObp.setSolicitudObtenida(true);
                Gson serializer = new Gson();
                return serializer.toJson(solicitudHistoriaObp);
            } catch (Exception ex) {
                Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                System.out.println("Numero de datos a persistir " + historico.size());
                long tiempoFinal = new Date().getTime();
                System.out.println("Tiempo de persistencia de datos " + (tiempoFinal - tiempoInicial) + "ms");
            }
        } else {
            SolicitudHistoriaObp solicitudHistoriaObp =  new SolicitudHistoriaObp();
            solicitudHistoriaObp.setTipoId(this.tipoId);
            solicitudHistoriaObp.setNumeroId(this.numeroId);
            solicitudHistoriaObp.setMensajesOBP(new HashSet<>());
            solicitudHistoriaObp.setEstadosEmisor(new HashSet<>());
            Gson serializer = new Gson();
            return serializer.toJson(solicitudHistoriaObp);
        }
        return null;
    }

    private static void cerrarSesion(HtmlPage paginaPrincipal) {
        long tiempoInicial = new Date().getTime();
        try {
            DomElement seccionPrincipal = paginaPrincipal.getElementById("PRINCIPAL");
            DomNodeList opciones = seccionPrincipal.getElementsByTagName("td");
            HtmlElement salida = (HtmlElement) opciones.get(1);
            salida.click();
        } catch (IOException ex) {
            System.out.println("No se encontró el elemento en la página principal");
            Logger.getLogger(ConsultarHistoriaLaboral.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            long tiempoFinal = new Date().getTime();
            System.out.println("Tiempo de logout " + (tiempoFinal - tiempoInicial) + "ms");
        }
    }

}
