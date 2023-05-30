package Helpers;

import android.os.Bundle;

import com.google.mlkit.vision.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Carnet {

    private List<String> listaBloque;
    private List<String> listaBloqueAnverso;
    private List<String> listaBloqueReverso;
    private Map<String, String> map;
    private Map<String, String> mapElimandos;
    private String texto;
    private final String[] MESES = {"febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre","enero"};
    private final String[] MESES_MENOS_EXACTOS = {"febrero", "marzo","junio", "agosto", "septiembre", "octubre", "noviembre", "diciembre","enero"};
    private final String[] DEPARTAMENTOS = {"cochabamba", "santa cruz", "oruro", "chuquisaca", "la paz", "beni", "pando", "tarija", "potosi"};
    //private final String[] ESTADO_CIVIL = {"soltera", "casada", "viuda", "divorciada"};
    //private final String[] OCUPACION = {"profesion/ocupacion"};
    //private final String[] DOMICILIO = {"domicilio"};
    private final String[] LIMPIADORES = {"servicio", "ident","general", "identificacion", "firma","personal","pertenece","fotografia","identidad","domicilio","certifica","fotografia", "impresion","pertenece","profesion/ocupacion","civil","estado","soltera", "casada", "viuda", "divorciada","documentos","registrados","cochabamba", "santa cruz", "oruro", "chuquisaca", "la paz", "beni", "pando", "tarija", "potosi"};
    private final  String[] CARACTERES = {"cedi","cedi","edula","iden","plurina","estado","profesi","ocupasc","lurina","urina","nacion","secci","urina","estad","bolivia","direc","direct","aliva","serie","plur","bio","sional","cional","denti","nalde","etado"};


    public Carnet() {
        this.texto = "";
        this.map = new HashMap<>();
        this.mapElimandos = new HashMap<>();
    }
    public void limpiarCaracteres() {
        for (String seccion : LIMPIADORES) {
            try {
                this.listaBloque = listaBloque.stream().filter(bloque -> (!bloque.toLowerCase().contains(seccion.toLowerCase()))).collect(Collectors.toList());
            } catch (Exception e) {
            }
        }
        for (String seccion : CARACTERES) {
            try {
                this.listaBloque = listaBloque.stream().filter(bloque -> (!bloque.toLowerCase().contains(seccion.toLowerCase()))).collect(Collectors.toList());
            } catch (Exception e) {
            }
        }
    }

    private void eliminarCadenasCortas() {
        try {
            this.listaBloque = listaBloque.stream().filter(bloque -> (bloque.length() >= 4)).collect(Collectors.toList());
        } catch (Exception e) {
        }
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = this.texto + texto;
    }

    public int levenshteinDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i][j - 1], dp[i - 1][j]));
                }
            }
        }

        return dp[len1][len2];
    }

    private String encontrarBloque(String[] secciones, int distanciaMaxima, List<String> listaBloque) {
        String bloqueEncontrado = "";
        for (String seccion : secciones) {
            for (String bloque : listaBloque) {
                String[] bloques = bloque.split(" ");
                if(!bloque.toLowerCase().contains(seccion.toLowerCase()) || !seccion.toLowerCase().contains(bloque.toLowerCase())){
                    for (String palabra : bloques) {
                        int distancia = levenshteinDistance(seccion.toLowerCase(), palabra.toLowerCase());
                        if (distancia <= distanciaMaxima  || seccion.toLowerCase().contains(bloque.toLowerCase())) {
                            bloqueEncontrado = bloque;
                            return bloqueEncontrado;
                        }
                    }
                }else {
                    bloqueEncontrado = bloque;
                    return bloqueEncontrado;
                }
            }
        }
        return bloqueEncontrado;
    }

    public String imprimir(Map<String, Object> map){
        String imprimir = "";
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String clave = entry.getKey().toString();
            String valor = entry.getValue().toString();
            imprimir += clave + ": " + valor +"\n";
        }
        return imprimir;
    }

    public void setListaBloqueAnverso(List<Text.TextBlock> listaBloques) {
        this.listaBloqueAnverso = extraerBloques(listaBloques);
    }

    public void setListaBloqueReverso(List<Text.TextBlock> listaBloques) {
        this.listaBloqueReverso = extraerBloques(listaBloques);
    }
    private List<String>  extraerBloques(List<Text.TextBlock> listaBloques){
        List<String> listaBloque = new ArrayList<>();
        for (int i = 0; i < listaBloques.size(); i++) {
            Text.TextBlock  bloque = listaBloques.get(i);
            String[] bloques = bloque.getText().split("\n");
            if(bloques.length>1){
                for (int j = 0; j < bloques.length; j++) {
                    listaBloque.add(bloques[j]);
                }
            }else{
                listaBloque.add(listaBloques.get(i).getText());
            }
        }
        return listaBloque;
    }

    public Bundle obtenerDatos() {
        Bundle datos = new Bundle();
        Bundle carnet = new Bundle();
        List<String> datosFiltradosAnverso = filtrar(listaBloqueAnverso,CARACTERES);
        String extencion = encontrarBloque(DEPARTAMENTOS,1,datosFiltradosAnverso);
        datosFiltradosAnverso = filtrarTamanios(datosFiltradosAnverso,6);
        List<String> vencimiento = encontrarFechas(datosFiltradosAnverso);

        List<String> listaNumerosAnverso = ListaNumeros(datosFiltradosAnverso,5);
        List<String> datosFiltradosReverso = filtrar(listaBloqueReverso,CARACTERES);
        datosFiltradosReverso = filtrarTamanios(datosFiltradosReverso,4);
        datosFiltradosReverso = filtrar(datosFiltradosReverso,LIMPIADORES);
        datos.putString("FECHA_NACIMIENTO",fecha(encontrarBloque(MESES,1,datosFiltradosReverso)));
        datosFiltradosReverso = filtrar(datosFiltradosReverso,MESES_MENOS_EXACTOS);
        List<String> listaCadenasLargas = filtrarTamanios(datosFiltradosReverso,10);
        List<String> listaNumerosReverso = ListaNumeros(datosFiltradosReverso,5);

        String nroCarnet = listaNumerosAnverso.get(0);

        datos = obtenerNroCarnet(vencimiento,extencion,nroCarnet,datos);
        listaCadenasLargas = filtrarNumeros(listaCadenasLargas);
        datos = obtenerNombre(listaCadenasLargas, datos);
        return datos;
    }

    private List<String> encontrarFechas(List<String> datosFiltradosAnverso) {
        List<String> bloqueEncontrado = new ArrayList<>();
        for (String seccion : MESES) {
            for (String bloque : datosFiltradosAnverso) {
                String[] bloques = bloque.split(" ");
                if(!bloque.toLowerCase().contains(seccion.toLowerCase())){
                    for (String palabra : bloques) {
                        int distancia = levenshteinDistance(seccion.toLowerCase(), palabra.toLowerCase());
                        if (distancia <= 0) {
                            bloqueEncontrado.add(bloque);
                        }
                    }
                }else {
                    bloqueEncontrado.add(bloque);
                }
            }
        }
        return bloqueEncontrado;
    }

    private List<String> filtrarNumeros(List<String> listaCadenasLargas) {
        List<String> filtrarNumeros = new ArrayList<>();
        int cantidad =0;
        for (String bloques: listaCadenasLargas){
            String[] bloque = bloques.split(" ");
            for (String cadena :bloque) {
                if (esNumero(cadena)){
                    cantidad++;
                }

            }
            if(cantidad == 0){
                filtrarNumeros.add(bloques);
                cantidad = 0;
            }
            cantidad = 0;
        }
        return filtrarNumeros;
    }

    private Bundle obtenerNombre(List<String> bloques, Bundle datos) {
        String nombre = "";
        int cadenasMayoresAtresCaracteres = 0;
        for (String posiblesNombres : bloques) {
            String[] posibleNombre = posiblesNombres.split(" ");
            if(posibleNombre.length>=3){
                for (String cadena : posibleNombre) {
                    if (cadena.length()>=3) {
                        cadenasMayoresAtresCaracteres++;
                    }
                }
                if (posibleNombre.length==cadenasMayoresAtresCaracteres){
                    nombre = posiblesNombres;
                    break;
                }else{
                    cadenasMayoresAtresCaracteres = 0;
                }
            }
        }

        String[] bloque = nombre.split(" ");
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(bloque));
        String apellidoCasada = "";
        String primerNombre = "";
        String segundoNombre = "";
        String apellidoPaterno = "";
        String apellidoMaterno = "";
        String unApellido = "";

        int posicion = -1;
        if(bloque.length==4){
            primerNombre = arrayList.get(0);
            segundoNombre = arrayList.get(1);
            apellidoPaterno = arrayList.get(2);
            apellidoMaterno = arrayList.get(3);
        }
        if(bloque.length==3){
            primerNombre = arrayList.get(0);
            apellidoPaterno = arrayList.get(1);
            apellidoMaterno = arrayList.get(2);
        }
        int posicionUno = 0;
        int posicionDos = 0;
        int posicionTres = 0;
        int posicionCasada = 0;
        if(bloque.length>4){
            for (int i = 0; i < bloque.length; i++) {

                if(bloque[i].toLowerCase().equals("de")){
                    if(bloque[i+1].toLowerCase().equals("la") || bloque[i+1].toLowerCase().equals("a") || bloque[i+1].toLowerCase().equals("los") ){
                        try {
                            apellidoPaterno = bloque[i+0] + " " + bloque[i+1]+ " " + bloque[i+2];
                            apellidoMaterno = bloque[i+3];
                            posicionUno = i;
                        }catch (Exception e){
                            apellidoMaterno = bloque[i+0] + " " + bloque[i+1]+ " " + bloque[i+2];
                            apellidoPaterno = bloque[i-1];
                            posicionUno = i;
                        }
                        unApellido = bloque[i+0] + " " + bloque[i+1]+ " " + bloque[i+2];
                        posicionUno = i;
                    }else {
                        apellidoMaterno = bloque[i-1];
                        apellidoPaterno = bloque[i-2];
                        apellidoCasada = bloque[i+1];
                        posicionCasada = i;
                    }
                }else{
                    if(bloque[i].toLowerCase().equals("del")){
                        unApellido = bloque[i+0] + " " + bloque[i+1];
                        posicionUno = i;

                    }
                }
            }
            if(posicionUno==0){
                primerNombre = bloque[0];
                segundoNombre = bloque[1]+" "+bloque[2];
                apellidoPaterno = bloque[3];
                apellidoMaterno = bloque[4];
            }
            primerNombre = bloque[0];
            segundoNombre = bloque[1];
        }

        datos.putString("AP_CASADA",apellidoCasada);
        datos.putString("AP_PATERNO",apellidoPaterno);
        datos.putString("AP_MATERNO",apellidoMaterno);
        datos.putString("PRIMER_NOMBRE",primerNombre);
        datos.putString("SEGUNDO_NOMBRE",segundoNombre);

        return datos;
    }

    private Bundle obtenerNroCarnet(List<String>  vencimiento, String extencion, String nroCarnet, Bundle datos) {
        datos.putString("VENCIMIENTO",determinarFechaVencimiento(vencimiento));
        datos.putString("EXTENSION",extencion(extencion));
        datos = nroCarnet(nroCarnet,datos);
        return datos;
    }

    private String determinarFechaVencimiento(List<String> vencimiento) {
        String fecha = "";
        if (vencimiento.size()==1){
            fecha = fecha(vencimiento.get(0));
        }
        if (vencimiento.size()==2){
            String[] bloquesF1 = vencimiento.get(0).split(" ");
            String[] bloquesF2 = vencimiento.get(1).split(" ");

            for (int i = 0; i < bloquesF1.length; i++) {
                try {
                        int numero = Integer.parseInt(bloquesF1[i]);
                        if (numero < 31){
                            fecha = bloquesF1[i];
                        }
                }catch (Exception e){
                    try {
                        int numero = Integer.parseInt(bloquesF2[i]);
                        if (numero < 31){
                            fecha = bloquesF2[i];
                        }
                    }catch (Exception ex){

                    }
                }
            }
            for (String bloque :bloquesF1) {
                for (String mes : MESES) {
                    if (levenshteinDistance(bloque.toLowerCase(),mes.toLowerCase())<=1){
                        switch (mes.toLowerCase()){
                            case "enero":
                                fecha += "/01";
                                break;
                            case "febrero":
                                fecha += "/02";
                                break;
                            case "marzo":
                                fecha += "/03";
                                break;
                            case "abril":
                                fecha += "/04";
                                break;
                            case "mayo":
                                fecha += "/05";
                                break;
                            case "junio":
                                fecha += "/06";
                                break;
                            case "julio":
                                fecha += "/07";
                                break;
                            case "agosto":
                                fecha += "/08";
                                break;
                            case "septiembre":
                                fecha += "/09";
                                break;
                            case "octubre":
                                fecha += "/10";
                                break;
                            case "noviembre":
                                fecha += "/11";
                                break;
                            case "diciembre":
                                fecha += "/12";
                                break;
                        }
                    }
                }
            }
            for (int i = 0; i < bloquesF1.length; i++) {
                try {
                    int numero = Integer.parseInt(bloquesF1[i]);
                        if (numero > 1500){
                            fecha += "/"+bloquesF2[i];
                        }
                }catch (Exception e){






                    if(esNumero(bloquesF2[i])){
                        int numero = Integer.parseInt(bloquesF2[i]);
                        if (numero > 1500){
                            fecha += "/"+bloquesF2[i];
                        }
                    }
                }
            }
        }
        return fecha;
    }

    private Bundle nroCarnet(String nroCarnet, Bundle datos) {
        String[] bloques = nroCarnet.split(" ");
        String carnetDepurado = "";
        for (String cadena :
                bloques) {
            if(!cadena.toLowerCase().contains("n")){
                carnetDepurado+=cadena;
            }
        }

        bloques = carnetDepurado.split("-");

        if (bloques.length>=2){
            datos.putString("nro",bloques[0]);
            datos.putString("complemento",bloques[1]);
        }else {
            datos.putString("nro",bloques[0]);
            datos.putString("complemento","");
        }

        return datos;
    }

    private String extencion(String extencion) {
        String[] bloques = extencion.split(" ");
        String resultado = "";
        for (String bloque :bloques) {
            bloque = bloque.toLowerCase();
            if(bloque.equals("")){
                return resultado;
            }
            if (levenshteinDistance(bloque,"cochabamba")<=1){
                return "CB";
            }
            if (levenshteinDistance(bloque,"la paz")<=1 || "la paz".contains(bloque)){
                return "LP";
            }
            if (levenshteinDistance(bloque,"oruro")<=1){
                return "OR";
            }
            if (levenshteinDistance(bloque,"pando")<=1){
                return "PA";
            }
            if (levenshteinDistance(bloque,"potosi")<=1){
                return "PO";
            }
            if (levenshteinDistance(bloque,"santa cruz")<=1 || "santa cruz".contains(bloque)){
                return "SC";
            }
            if (levenshteinDistance(bloque,"tarija")<=1){
                return "TJ";
            }if (levenshteinDistance(bloque,"beni")<=1){
                return "BE";
            }
            if (levenshteinDistance(bloque,"chuquisaca")<=1){
                return "CH";
            }
        }
        return resultado;
    }

    private String fecha(String vencimiento) {
        String[] bloques = vencimiento.split(" ");
        String fecha = "";

        for (String bloque :bloques) {
            try {
                int numero = Integer.parseInt(bloque);
                if (numero < 31){
                    fecha = bloque;
                }
            }catch (Exception e){

            }
        }
        for (String bloque :bloques) {
            for (String mes : MESES) {
                if (levenshteinDistance(bloque.toLowerCase(),mes.toLowerCase())<=1){
                    switch (mes.toLowerCase()){
                        case "enero":
                            fecha += "/01";
                            break;
                        case "febrero":
                            fecha += "/02";
                            break;
                        case "marzo":
                            fecha += "/03";
                            break;
                        case "abril":
                            fecha += "/04";
                            break;
                        case "mayo":
                            fecha += "/05";
                            break;
                        case "junio":
                            fecha += "/06";
                            break;
                        case "julio":
                            fecha += "/07";
                            break;
                        case "agosto":
                            fecha += "/08";
                            break;
                        case "septiembre":
                            fecha += "/09";
                            break;
                        case "octubre":
                            fecha += "/10";
                            break;
                        case "noviembre":
                            fecha += "/11";
                            break;
                        case "diciembre":
                            fecha += "/12";
                            break;
                    }
                }
            }
        }
        for (String bloque :bloques) {
            try {
                int numero = Integer.parseInt(bloque);
                if (numero > 1500){
                    fecha += "/"+bloque;
                }
            }catch (Exception e){

            }
        }
        return fecha;
    }

    private String obtenerNroCarnet(List<String> listaNumerosAnverso, List<String> listaNumerosReverso) {
        for (String bloquesAnverso : listaNumerosAnverso) {
            String[] bloquesAn = bloquesAnverso.split(" ");
            for (String bloqueAnverso : bloquesAn) {
                if(listaNumerosReverso.size()!=0){
                    for (String bloquesReverso : listaNumerosReverso) {
                        String[] bloquesRe = bloquesReverso.split(" ");
                        for (String bloqueReverso: bloquesRe) {
                            int distancia = levenshteinDistance(bloqueAnverso,bloqueReverso);
                            if(distancia <= 2){
                                return bloqueAnverso;
                            }
                        }
                    }
                }else{
                    if(esNumero(bloqueAnverso) && bloqueAnverso.length()>6){
                        return bloqueAnverso;
                    }
                }
            }
        }
        return "";
    }

    private List<String> ListaNumeros(List<String> listaBloque, int cantMinimaNumeros) {
        int cantidad = 0;
        int distancia = 0;
        List<String> listaNumeros = new ArrayList<>();
        try {
            for (String bloque : listaBloque) {
                String[] cadenas = bloque.split(" ");
                for (String cadena : cadenas) {
                    if(esNumero(cadena)){
                        listaNumeros.add(bloque);
                    }
                }
            }
        }catch (Exception e){

        }
        return listaNumeros;
    }

    private boolean esNumero(String cadena) {
        int cantidad = 0;
        int distancia = 0;
        for (int i = 0; i < cadena.length(); i++) {
            try {
                int numero = Integer.parseInt(String.valueOf(cadena.charAt(i)));
                cantidad++;
            }catch (Exception e){
                if(distancia>4){
                    return false;
                }else {
                    distancia++;
                }
            }
        }
        if(cantidad >= 5){
            cantidad = 0;
            return true;
        }
        return false;
    }

    private List<String> filtrarTamanios(List<String> listaBloque,int tamanio) {
        try {
            listaBloque = listaBloque.stream().filter(bloque -> (bloque.length() >= tamanio)).collect(Collectors.toList());
        } catch (Exception e) {
        }
        return listaBloque;
    }

    private List<String> filtrar(List<String> listaBloque, String[] filtros) {
        for (String filtro : filtros) {
            try {
                listaBloque = listaBloque.stream().filter(bloque -> (!bloque.toLowerCase().contains(filtro.toLowerCase()))).collect(Collectors.toList());
            } catch (Exception e) {
            }
        }
        return listaBloque;
    }
}