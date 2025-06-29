

<!-- Refiere a la tag library core -->
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css">
<link type="text/css" rel="stylesheet" href="css/estilos.css">
<meta charset="ISO-8859-1">
<title>Granja System</title>
</head>
<body>
<!-- NAV -->




	<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
      <a class="navbar-brand" href="GranjaControlador?command=LIST">Granja ChickenTest</a>
      <div class="container">
	      <a class="nav-link"><button type="button" class="btn btn-success mr-4 ">Saldo: $</button></a>
		  <a class="nav-link" href="comprar-articulo.jsp"><button type="button" class="btn btn-light mr-4">Comprar</button></a>
		  <a class="nav-link" href="vender-articulo.jsp"><button type="button" class="btn btn-light mr-4">Vender</button></a>
		  <a class="nav-link" href="GranjaControlador?command=REPORTE"><button type="button" class="btn btn-light mr-4">Reporte</button></a>
		  <a class="nav-link" href="GranjaControlador?command=LOGOUT" onclick="if (!(confirm('Estas seguro de cerrar la sesión?'))) return false"><button type="button" class="btn btn-light">Salir</button></a>
	  </div>
    </nav>
<!-- NAV -->
	<br/>
	<br/>
	
		<form action="GranjaControlador" method="GET">
			<input type="hidden" name="command" value="INSERT"/>
			<div class="container col-6">
			<h3 class="d-flex justify-content-center"> Agregar artículo </h3>
			<br/>
							<div class="form-group">
	 							<label>Nombre:</label>
	  							<input type="text" class="form-control" name="nombre">
							</div>
							<div class="form-group">
	 							<label>Categoría:</label>
	  							<input type="text" class="form-control" name="categoria">
							</div>
							<div class="form-group">
	 							<label>Edad:</label>
	  							<input type="text" class="form-control" name="edad">
							</div>
							<div class="form-group">
	 							<label>Unidades:</label>
	  							<input type="text" class="form-control" name="unidades">
							</div>
							<div class="form-group">
	 							<label>Stock:</label>
	  							<input type="text" class="form-control" name="stock">
							</div>
							<div class="form-group">
	 							<label>Precio:</label>
	  							<input type="text" class="form-control" name="precio">
							</div>
							<div class="form-group">
	 							<label>Origen:</label>
	  							<input type="text" class="form-control" name="produccion">
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