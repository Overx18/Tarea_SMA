import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class AgenteRestaurante extends Agent {

    @Override
    protected void setup() {
        System.out.println("Restaurante " + getLocalName() + " iniciado.");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("restaurante");
        sd.setName("ServicioComida");
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
                    System.out.println("Restaurante " + getLocalName() + " recibió un pedido de " + msg.getSender().getLocalName());

                    AID agentePago = buscarAgente("pago");
                    AID repartidor = buscarAgente("repartidor");

                    if (agentePago != null && repartidor != null) {
                        ACLMessage pagoReq = new ACLMessage(ACLMessage.REQUEST);
                        pagoReq.addReceiver(agentePago);
                        pagoReq.setContent("Yapeo recibido de " + msg.getSender().getLocalName());
                        send(pagoReq);

                        ACLMessage pagoResp = blockingReceive();
                        if (pagoResp != null && pagoResp.getPerformative() == ACLMessage.CONFIRM) {
                            System.out.println("Restaurante " + getLocalName() + " recibió confirmación de pago de " + agentePago.getLocalName());

                            ACLMessage orden = new ACLMessage(ACLMessage.REQUEST);
                            orden.addReceiver(repartidor);
                            orden.setContent("Arribado en domicilio de " + msg.getSender().getLocalName());
                            send(orden);
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



