package monitor;

import conexion.Conexion;
import mensaje.Mensaje;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class Monitor implements Runnable{

	private static final int INTERVALO_HEARTBEAT = 4000;
	public static final int puerto = 5555;
    public Conexion conexionMonitor;

    public Monitor(){
    }

    @Override
    public void run() {

        try {

            this.conexionMonitor = new Conexion();
            //ServerSocket
            conexionMonitor.establecerConexion(puerto);
            
            Mensaje mensaje;
            String msg;
            int puertoRecibido;
            
    		Timer timer = new Timer();
    		HeartbeatCheckTask task = new HeartbeatCheckTask(this);
            timer.schedule(task, 0, INTERVALO_HEARTBEAT);
            
            while (true){

                this.conexionMonitor.aceptarConexion();
                
                mensaje = this.recibeMensaje();
                msg = mensaje.getMensaje();
                puertoRecibido = Integer.parseInt(msg);

                if (puertoRecibido == 9090)
                	task.reciboHeartBeatUno();
                else 
                	task.reciboHeartBeatDos();
  
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Conexion getConexion() {
        return this.conexionMonitor;
    }

    public Mensaje recibeMensaje() {
        ObjectInputStream in = this.conexionMonitor.getInputStreamConexion();
        try {
            return (Mensaje) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    static class HeartbeatCheckTask extends TimerTask {
        public boolean heartbeatServidorUno=false;
        public boolean heartbeatServidorDos=false;
        public boolean primera = true;
        public Monitor monitor;

        public HeartbeatCheckTask(Monitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public void run() {
            ObjectOutputStream out;
            Conexion conexion = monitor.getConexion();
        	if(!primera) {
	            if (heartbeatServidorUno) {
	                System.out.println("Servidor Uno Activo");
	                // Realizar la acción correspondiente cuando se recibe el heartbeat correctamente
	                
	                // Reiniciar la variable para el siguiente chequeo
	                heartbeatServidorUno = false;
	            } else {
	                System.out.println("Servidor Uno perdido");
	                // Realizar la acción correspondiente cuando se pierde el heartbeat
                    try {
                        conexion.crearConexionEnvio("localhost", 8888);
                        out = new ObjectOutputStream(conexion.getSocket().getOutputStream());
                        Mensaje mensajeClienteServidor = new Mensaje();
                        mensajeClienteServidor.setMensaje("SOS DE BOCA");
                        out.writeObject(mensajeClienteServidor);
                        out.close();
                        conexion.cerrarConexion();
                    }
                    catch(Exception ex) {
                        //e.printStackTrace();
                    }
	                try {
                        Runtime.getRuntime().exec("java -jar ServerUno_jar/Server.jar");
					} catch (Exception e) {
                        e.printStackTrace();
						System.out.println("No se encontro el ejecutable del servidor 1");
					}
	            }

	            
	            if (heartbeatServidorDos) {
	                System.out.println("Servidor Dos Activo");
	                // Realizar la acción correspondiente cuando se recibe el heartbeat correctamente
	                
	                // Reiniciar la variable para el siguiente chequeo
	                heartbeatServidorDos = false;
	            } else {
	                System.out.println("Servidor Dos perdido");
	                // Realizar la acción correspondiente cuando se pierde el heartbeat
                    try {

                        conexion.crearConexionEnvio("localhost", 9090);
                        out = new ObjectOutputStream(conexion.getSocket().getOutputStream());
                        Mensaje mensajeClienteServidor = new Mensaje();
                        mensajeClienteServidor.setMensaje("SOS DE BOCA");
                        out.writeObject(mensajeClienteServidor);
                        out.close();
                        conexion.cerrarConexion();
                    }
                    catch(Exception ex) {
                        //e.printStackTrace();
                    }
	                try {
                        Runtime.getRuntime().exec("java -jar ServerDos_jar/Server.jar");
					} catch (Exception e) {
                        e.printStackTrace();
						System.out.println("No se encontro el ejecutable del servidor 2");
					}
	            }
        	}
        	else
        		primera=false;
        }
        
        public void reciboHeartBeatUno() {
        	this.heartbeatServidorUno=true;
        }
        
        public void reciboHeartBeatDos() {
        	this.heartbeatServidorDos = true;
    }
  }


}