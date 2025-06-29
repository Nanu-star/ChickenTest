package com.chickentest.web;

public class Usuarios {
	String usuario;
	String contraseña;
	double saldo;
	public Usuarios(String usuario, String contraseña) {
		super();
		this.usuario = usuario;
		this.contraseña = contraseña;
	}
	public Usuarios(String usuario, String contraseña, double saldo) {
		super();
		this.usuario = usuario;
		this.contraseña = contraseña;
		this.saldo = saldo;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getContraseña() {
		return contraseña;
	}
	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}
	public double getSaldo() {
		return saldo;
	}
	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}
	@Override
	public String toString() {
		return "Usuarios [usuario=" + usuario + ", contraseña=" + contraseña + ", saldo=" + saldo + "]";
	}

	
	
}
