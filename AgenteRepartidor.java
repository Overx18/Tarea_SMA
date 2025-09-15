import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class AgenteRepartidor extends Agent {

    @Override
    protected void setup() {
        System.out.println("Repartidor " + getLocalName() + " iniciado.");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("repartidor");
        sd.setName("ServicioReparto");
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
                    String content = msg.getContent(); // Ejemplo: "Entregar chifa a Diego"
                    System.out.println("Repartidor " + getLocalName() + " recibe orden de Central: " + content);

                    String cliente = content.substring(content.lastIndexOf(" ") + 1);

                    ACLMessage confirmacion = new ACLMessage(ACLMessage.INFORM);
                    confirmacion.addReceiver(new AID(cliente, AID.ISLOCALNAME));
                    confirmacion.setContent("Pedido entregado por " + getLocalName());
                    send(confirmacion);
                } else {
                    block();
                }
            }
        });
    }
}



