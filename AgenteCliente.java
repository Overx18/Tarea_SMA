/*
 * 
 * javac -d out "-cp" "..\\jade.jar" *.java
 * java -cp "..\\jade.jar;out" jade.Boot -gui "Diego:AgenteCliente;Central:AgenteRestaurante;Yonaiker:AgenteRepartidor;Yape:AgentePago"
 *
 *  java -cp "..\\jade.jar;out" jade.Boot -gui "Diego:AgenteCliente(reserva:pollo);Central:AgenteRestaurante;Yape:AgentePago;Yonaiker:AgenteRepartidor"
 * java -cp "..\\jade.jar;out" jade.Boot -gui "Diego:AgenteCliente(pedido:chifa);Central:AgenteRestaurante;Yape:AgentePago;Yonaiker:AgenteRepartidor"
 */

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class AgenteCliente extends Agent {

    @Override
    protected void setup() {
        System.out.println("Cliente " + getLocalName() + " iniciado.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String[] parts = args[0].toString().split(":");
            if (parts.length == 2) {
                String tipo = parts[0];
                String plato = parts[1];

                addBehaviour(new WakerBehaviour(this, 2000) {
                    @Override
                    protected void onWake() {
                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();
                        sd.setType("restaurante");
                        template.addServices(sd);

                        try {
                            DFAgentDescription[] result = DFService.search(myAgent, template);
                            if (result.length > 0) {
                                AID restaurante = result[0].getName();

                                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                                msg.addReceiver(restaurante);
                                msg.setContent(tipo + ":" + plato);
                                send(msg);

                                if (tipo.equalsIgnoreCase("pedido")) {
                                    System.out.println("Cliente " + getLocalName() + " solicitó un pedido de " + plato + " a " + restaurante.getLocalName());
                                } else {
                                    System.out.println("Cliente " + getLocalName() + " solicitó una reserva de " + plato + " a " + restaurante.getLocalName());
                                }
                            } else {
                                System.out.println("Cliente " + getLocalName() + ": No se encontró ningún restaurante en DF.");
                            }
                        } catch (FIPAException fe) {
                            fe.printStackTrace();
                        }
                    }
                });

                addBehaviour(new CyclicBehaviour(this) {
                    @Override
                    public void action() {
                        ACLMessage msg = receive();
                        if (msg != null) {
                            String content = msg.getContent();
                            if (content.contains("Reserva confirmada")) {
                                System.out.println("Cliente " + getLocalName() + " se le informa: " + content);
                            } else if (content.contains("Pedido entregado")) {
                                System.out.println("Cliente " + getLocalName() + " confirma que recibió el pedido.");
                            }
                        } else {
                            block();
                        }
                    }
                });

            } else {
                System.out.println("Formato de argumento inválido. Use: pedido:plato o reserva:plato");
            }
        } else {
            System.out.println("No se pasó argumento. Ejemplo: pedido:chifa o reserva:pollo");
        }
    }
}





