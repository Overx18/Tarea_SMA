import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class AgenteRestaurante extends Agent {

    @Override
    protected void setup() {
        System.out.println("Restaurante " + getLocalName() + " iniciado.");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("restaurante");
        sd.setName("DeliveryComida");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.REQUEST) {
                    String contenido = msg.getContent(); // Ej: "pedido:chifa" o "reserva:pollo"
                    String[] parts = contenido.split(":");

                    if (parts.length == 2) {
                        String tipo = parts[0];
                        String producto = parts[1];
                        String cliente = msg.getSender().getLocalName();

                        System.out.println("Restaurante " + getLocalName() + " recibió un " + tipo + " de " + producto + " de " + cliente);

                        AID agentePago = buscarAgente("pago");

                        if (agentePago != null) {
                            ACLMessage pagoReq = new ACLMessage(ACLMessage.REQUEST);
                            pagoReq.addReceiver(agentePago);
                            pagoReq.setContent(tipo + ":" + producto + ":" + cliente);
                            send(pagoReq);

                            ACLMessage pagoResp = blockingReceive();
                            if (pagoResp != null && pagoResp.getPerformative() == ACLMessage.CONFIRM) {
                                System.out.println("Restaurante " + getLocalName() + " recibió confirmación de pago de " + pagoResp.getSender().getLocalName());

                                if (tipo.equalsIgnoreCase("pedido")) {
                                    AID repartidor = buscarAgente("repartidor");
                                    if (repartidor != null) {
                                        ACLMessage orden = new ACLMessage(ACLMessage.REQUEST);
                                        orden.addReceiver(repartidor);
                                        orden.setContent("Entregar " + producto + " a " + cliente);
                                        send(orden);
                                    }
                                } else if (tipo.equalsIgnoreCase("reserva")) {
                                    ACLMessage confirm = new ACLMessage(ACLMessage.INFORM);
                                    confirm.addReceiver(msg.getSender());
                                    confirm.setContent("Reserva confirmada para " + producto + " en " + getLocalName());
                                    send(confirm);
                                }
                            }
                        }
                    }
                } else {
                    block();
                }
            }
        });
    }

    private AID buscarAgente(String tipo) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(tipo);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                return result[0].getName();
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        return null;
    }
}






