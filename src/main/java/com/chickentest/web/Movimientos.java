package com.chickentest.web;

import java.util.Date;

public class Movimientos {

	public Movimientos(String articulo, double monto, String username) {
		super();
		this.articulo = articulo;
		this.monto = monto;
		this.username = username;
	}
	
	public Movimientos(String articulo, Date fecha, String tipo, double monto, String username) {
		super();
		this.articulo = articulo;
		this.fecha = fecha;
		this.tipo = tipo;
		this.monto = monto;
		this.username = username;
	}

	public String getArticulo() {
		return articulo;
	}
	public void setArticulo(String articulo) {
		this.articulo = articulo;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public double getMonto() {
		return monto;
	}
	public void setMonto(double monto) {
		this.monto = monto;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	String articulo;
	Date fecha;
	String tipo;
	double monto;
	String username;


}
