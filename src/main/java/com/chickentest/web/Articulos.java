package com.chickentest.web;

import java.util.Date;

public class Articulos {
	int id;
	int unidades;
	String nombre;
	double precio;
	int edad;
	String categoria;
	int stock;
	String produccion;
	Date creacion;
	private String precioS;
	
	public Articulos(int unidades, String nombre, double precio, String categoria, int stock, String produccion, Date creacion) {
		super();
		this.unidades = unidades;
		this.nombre = nombre;
		this.precio = precio;
		this.categoria = categoria;
		this.stock = stock;
		this.produccion = produccion;
		this.creacion = creacion;
	}
	public Articulos(int id, int unidades, String nombre, double precio, String categoria, int stock, String produccion, Date creacion) {
		super();
		this.id = id;
		this.unidades = unidades;
		this.nombre = nombre;
		this.precio = precio;
		this.categoria = categoria;
		this.stock = stock;
		this.produccion = produccion;
		this.creacion = creacion;
	}
	public Articulos(int id, int unidades, String nombre, double precio, String categoria, int edad, int stock, String produccion, Date creacion) {
		super();
		this.id = id;
		this.unidades = unidades;
		this.nombre = nombre;
		this.precio = precio;
		this.categoria = categoria;
		this.edad = edad;
		this.stock = stock;
		this.produccion = produccion;
		this.creacion = creacion;
	}
	public Articulos(int unidades, String nombre, double precio, String categoria, int edad, int stock, String produccion, Date creacion, String precioS) {
		super();
		this.unidades = unidades;
		this.nombre = nombre;
		this.precio = precio;
		this.categoria = categoria;
		this.edad = edad;
		this.stock = stock;
		this.produccion = produccion;
		this.creacion = creacion;
		this.precioS = precioS;
	}
	public int getEdad() {
		return edad;
	}
	public void setEdad(int edad) {
		this.edad = edad;
	}
	public Articulos() {
		// TODO Auto-generated constructor stub
	}
	public Articulos(int id, int unidades, String nombre, double precio, String categoria, int edad, int stock, String produccion, Date creacion, String precioS) {
		super();
		this.id = id;
		this.unidades = unidades;
		this.nombre = nombre;
		this.precio = precio;
		this.categoria = categoria;
		this.edad = edad;
		this.stock = stock;
		this.produccion = produccion;
		this.creacion = creacion;
		this.precioS = precioS;
	}
	public String getProduccion() {
		return produccion;
	}
	public void setProduccion(String produccion) {
		this.produccion = produccion;
	}
	public Date getCreacion() {
		return creacion;
	}
	public void setCreacion(Date creacion) {
		this.creacion = creacion;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUnidades() {
		return unidades;
	}
	public void setUnidades(int unidades) {
		this.unidades = unidades;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public double getPrecio() {
		return precio;
	}
	public void setPrecio(double precio) {
		this.precio = precio;
	}
	public String getCategoria() {
		return categoria;
	}
	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	public String getPrecioS() {
		return precioS;
	}
	public void setPrecioS(String precioS) {
		this.precioS = precioS;
	}
	
	
	}
