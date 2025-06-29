package com.chickentest.web;

public class Usuarios {
	String usuario;
	String contrase�a;
	double saldo;
	public Usuarios(String usuario, String contrase�a) {
		super();
		this.usuario = usuario;
		this.contrase�a = contrase�a;
	}
	public Usuarios(String usuario, String contrase�a, double saldo) {
		super();
		this.usuario = usuario;
		this.contrase�a = contrase�a;
		this.saldo = saldo;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getContrase�a() {
		return contrase�a;
	}
	public void setContrase�a(String contrase�a) {
		this.contrase�a = contrase�a;
	}
	public double getSaldo() {
		return saldo;
	}
	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}
	@Override
	public String toString() {
		return "Usuarios [usuario=" + usuario + ", contrase�a=" + contrase�a + ", saldo=" + saldo + "]";
	}

	
	
}
