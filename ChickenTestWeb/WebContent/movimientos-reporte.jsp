<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*,com.chickentest.web.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><!-- Refiere a la tag library core -->
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" >
<title>Granja System</title>
<!-- Refiere a la tag library core -->
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
<div class="row">
		<div class="col">
		<!-- botón de agregar que llama al formulario -->
		
			<table class="table table-striped">
				<thead class="thead-light">
				<tr>
					<th scope="col">Cantidades Actuales</th>
					<th scope="col"></th>
				</tr>
				</thead>
				<tbody>
				<tr>
						<td> Cantidad de Huevos</td>
						<td> ${REPORTE.huevos}</td>
				</tr>
				<tr>
						<td> Cantidad de Gallinas</td>
						<td> ${REPORTE.gallinas}</td>
				</tr>
				<tr/>
				</tbody>
				
				<thead class="thead-light">
				<tr>
					 <th scope="col">Ingreso total</th>
					 <th scope="col">$ ${REPORTE.montoVentas}</th>
				</tr>
				</thead>
				<tbody>
				<tr>
						<td> Maples vendidos</td>
						<td> ${REPORTE.cantLotesV}</td>
				</tr>
				<tr>
						<td> Gallinas vendidas</td>
						<td> ${REPORTE.cantGallinasV}</td>
				</tr>
				<tr/>
				</tbody>
				<thead class="thead-light">
				<tr>
					 <th scope="col">Gasto total</th>
					<th scope="col">$ ${REPORTE.montoCompras}</th>
				</tr>
				</thead>
				<tbody>
				<tr>
						<td> Maples comprados</td>
						<td>${REPORTE.cantLotesC}</td>
				</tr>
				<tr>
						<td> Gallinas compradas</td>
						<td>${REPORTE.cantGallinasC}</td>
				</tr>
				<tr/>
				</tbody>		
				<thead class="thead-light">
				<tr>
					 <th scope="col">Otros</th>
					 <th scope="col"></th>
				</tr>
				</thead>
				<tbody>
				<tr>
						<td> Lotes producidos</td>
						<td> ${REPORTE.cantLotesProducidos}</td>
				</tr>
				<tr>
						<td> Gallinas producidas</td>
						<td> ${REPORTE.gallinasNacidas}</td>
				</tr>
				<tr>
						<td> Gallinas fallecidas</td>
						<td> ${REPORTE.gallinasFallecidas}</td>
				</tr>
				<tr/>
				</tbody>
			</table>
		</div>
	</div>
</body>
</html>