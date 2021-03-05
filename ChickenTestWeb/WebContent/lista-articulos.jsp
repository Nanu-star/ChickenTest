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
	
<!-- Cuerpo -->
	<div class="row">
		<div class="col">
		<!-- botón de agregar que llama al formulario -->
		
			<table class="table table-striped">
				<thead class="thead-light">
				<tr>
					<th scope="col">Nombre</th>
					<th scope="col">Categoría</th>
					<th scope="col">Edad</th>
					<th scope="col">Unidades</th>
					<th scope="col">Stock</th>
					<th scope="col">Precio</th>
					<th scope="col" class="d-flex justify-content-center">Acción</th>
				</tr>
				</thead>
				<tbody>
					<c:forEach var="tempArticulo" items="${LISTA_ARTICULOS}">
					<c:url var="tempLink" value="GranjaControlador">
						<c:param name="command" value="LOAD"/>
						<c:param name="articuloId" value="${tempArticulo.id}"/>
					</c:url>
					
					<c:url var="deleteLink" value="GranjaControlador">
						<c:param name="command" value="DELETE"/>
						<c:param name="articuloId" value="${tempArticulo.id}"/>
					</c:url>
				<tr>
						<td> ${tempArticulo.nombre}</td>
						<td> ${tempArticulo.categoria}</td>
						<td> ${tempArticulo.edad}  días</td>
						<td> ${tempArticulo.unidades}</td>
						<td> ${tempArticulo.stock}</td>
						<td> ${tempArticulo.precioS}</td>
						<td class="d-flex justify-content-center"><a href="${tempLink}"><button type="button" class="btn btn-success mr-1">Actualizar </button></a>
						 <a href="${deleteLink}" onclick="if (!(confirm('Estas seguro de eliminar este articulo?'))) return false"><button type="button" class="btn btn-danger">Eliminar</button></a></td>
				</tr>
				</c:forEach>
			</table>
			
			
			</div>
		</div>
	<a href="agregar-articulo.jsp"><input type="button" class="btn btn-primary btn-block mx-auto" style="width: 300px;" value="Agregar artículo"></a>
</body>
</html>