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
                    String cliente = msg.getContent().replace("Arribado en domicilio de ", "");
                    System.out.println("Repartidor " + getLocalName() + " entregando pedido a " + cliente);

                    // Confirmar al cliente que recibió el pedido
                    ACLMessage confirmacion = new ACLMessage(ACLMessage.INFORM);
                    confirmacion.addReceiver(new AID(cliente, AID.ISLOCALNAME));
                    confirmacion.setContent("Pedido entregado");
                    send(confirmacion);
                } else {
                    block();
                }
            }
        });
    }
}


