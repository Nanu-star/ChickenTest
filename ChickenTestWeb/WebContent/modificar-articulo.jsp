<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*,com.chickentest.web.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><!-- Refiere a la tag library core -->
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" >
<meta charset="ISO-8859-1">
<title>Granja System</title>
</head>
<body>
<!-- NAV -->
<c:url var="logOut" value="GranjaControlador">
		<c:param name="command" value="LOGOUT"/>
</c:url>
<c:url var="reporte" value="GranjaControlador">
		<c:param name="command" value="REPORTE"/>
</c:url>
<c:url var="comprar" value="GranjaControlador">
		<c:param name="command" value="COMPRA"/>
</c:url>
<c:url var="home" value="GranjaControlador">
		<c:param name="command" value="LIST"/>
</c:url>
	<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
      <a class="navbar-brand" href="${home}">Granja ChickenTest</a>
      <div class="container">
	      <a class="nav-link"><button type="button" class="btn btn-success mr-4 ">Saldo: $${USER.saldo}</button></a>
		  <a class="nav-link" href="comprar-articulo.jsp"><button type="button" class="btn btn-light mr-4">Comprar</button></a>
		  <a class="nav-link" href="vender-articulo.jsp"><button type="button" class="btn btn-light mr-4">Vender</button></a>
		  <a class="nav-link" href="${reporte}"><button type="button" class="btn btn-light mr-4">Reporte</button></a>
		  <a class="nav-link" href="${logOut}" onclick="if (!(confirm('Estas seguro de cerrar la sesión?'))) return false"><button type="button" class="btn btn-light">Salir</button></a>
	  </div>
    </nav>
<!-- NAV -->
	<br/>
	<br/>
	
		<form action="GranjaControlador" method="GET">
			<input type="hidden" name="command" value="UPDATE"/>
			<input type="hidden" name="articuloId" value="${EL_ARTICULO.id}"/>
			<div class="container col-6">
			<h3 class="d-flex justify-content-center"> Modificar Artículo </h3>
			<br/>
							<div class="form-group">
	 							<label>Nombre:</label>
	  							<input type="text" class="form-control" name="nombre" value="${EL_ARTICULO.nombre}">
							</div>
							<div class="form-group">
	 							<label>Categoría:</label>
	  							<input type="text" class="form-control" name="categoria" value="${EL_ARTICULO.categoria}">
							</div>
							<div class="form-group">
	 							<label>Edad:</label>
	  							<input type="text" class="form-control" name="edad" value="${EL_ARTICULO.edad}">
							</div>
							<div class="form-group">
	 							<label>Unidades:</label>
	  							<input type="text" class="form-control" name="unidades" value="${EL_ARTICULO.unidades}">
							</div>
							<div class="form-group">
	 							<label>Stock:</label>
	  							<input type="text" class="form-control" name="stock" value="${EL_ARTICULO.stock}">
							</div>
							<div class="form-group">
	 							<label>Precio:</label>
	  							<input type="text" class="form-control" name="precio" value="${EL_ARTICULO.precio}">
							</div>
							<div class="form-group">
	 							<label>Origen:</label>
	  							<input type="text" class="form-control" name="produccion" value="${EL_ARTICULO.produccion}">
							</div>
							<br/>
							<div class="d-flex justify-content-center">
							<input class="btn btn-primary mb-3"type="submit" value="Guardar" />
							</div>
			</div>
						
				
		</form>
		
		<div style="clear: both;"></div>

</body>
</html>