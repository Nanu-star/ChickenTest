<%@ page import="java.util.*,com.chickentest.web.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><!-- Refiere a la tag library core -->
<!-- Refiere a la tag library core -->
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">
<title>Insert title here</title>
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
	<div class="container col-6">
	<h3 class="d-flex justify-content-center"> Vender Articulo </h3>
	<br/>
	
		<form action="GranjaControlador" method="GET">
					<input type="hidden" name="command" value="VENTA"/>
					<select name="articuloId" class="custom-select">
					<c:forEach var="tempArticulo" items="${LISTA_ARTICULOS}">
					<option value="${tempArticulo.id}">${tempArticulo.nombre}</option>
					</c:forEach>
					</select>
					<br/>
					<br/>
					<div class="form-group">
				 		<label>Cantidad:</label>
				  		<input type="text" class="form-control" name="cantidad">
					</div>
					<div class="d-flex justify-content-center">
					<input class="btn btn-primary" type="submit" value="Seleccionar" onclick="if (!(confirm('Desea confirmar esta venta?'))) return false"/>
					</div>
					<br/>
					
			</form>
	</div>	
			<div style="clear: both;"></div>
</body>
</html>