<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*,com.chickentest.web.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><!-- Refiere a la tag library core -->
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">
<meta charset="ISO-8859-1">
<title>Granja System</title>
</head>
<body>
<div class="row d-flex justify-content-center mt-5">
	<aside class="col-sm-4">
<div class="card">
<article class="card-body">
<h4 class="card-title mb-4 mt-1">Iniciar sesión</h4>
	 <form action="GranjaControlador" method="GET">
	 <input type="hidden" name="command" value="LOGIN"/>
    <div class="form-group">
    	<label>Usuario</label>
        <input class="form-control" name="usuario">
    </div> <!-- form-group// -->
    <div class="form-group">
    	<label>Contraseña</label>
        <input class="form-control" name="password">
    </div> <!-- form-group// --> 
    <div class="form-group">
    <button type="submit" class="btn btn-primary btn-block">Ingresar</button>
    </div> <!-- form-group// -->                                                           
</form>
</article>
</div> <!-- card.// -->
</aside>
</div> 
</body>
</html>