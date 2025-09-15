/*
 * 
 * javac -d out "-cp" "jade.jar" *.java
 * java -cp "jade.jar;out" jade.Boot -gui "Diego:AgenteCliente;Central:AgenteRestaurante;Yonaiker:AgenteRepartidor;Yape:AgentePago"
 * 
 */

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class AgenteCliente extends Agent {

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String argumento = (String) args[0];

            // Si el argumento es "delivery"
            if (argumento.equalsIgnoreCase("delivery")) {
                ServiceDescription servicio = new ServiceDescription();
                // El servicio es apagar fuego
                servicio.setType("mandar delivery");
                // Busca quién ofrece ese servicio
                buscar(servicio, "delivery");
            }

            // Si el argumento es "reservacion"
            if (argumento.equalsIgnoreCase("reservacion")) {
                ServiceDescription servicio = new ServiceDescription();
                // El servicio es atrapar ladrones
                servicio.setType("hacer reservacion");
                buscar(servicio, "reservacion");
            }
        }

        System.out.println("Cliente " + getLocalName() + " iniciado.");

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                doWait(1000);

                AID restaurante = buscarAgente("restaurante");
                if (restaurante != null) {
                    ACLMessage pedido = new ACLMessage(ACLMessage.REQUEST);
                    pedido.addReceiver(restaurante);
                    pedido.setContent("Pedido de comida");
                    send(pedido);

                    System.out.println("Cliente " + getLocalName() + " solicitó un pedido a " + restaurante.getLocalName());
                } else {
                    System.out.println("No se encontró ningún restaurante en el DF.");
                }
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    if (msg.getContent().contains("Pedido entregado")) {
                        System.out.println("Cliente " + getLocalName() + " recibió su pedido de " + msg.getSender().getLocalName());
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
