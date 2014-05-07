/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import static java.awt.Frame.MAXIMIZED_BOTH;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JOptionPane;

/**
 *
 * @author fabinhosano
 */
public class ToPS_Visualization extends javax.swing.JFrame {

    /**
     * Creates new form ToPS_Visualization
     */
    public ToPS_Visualization() {
        try {
            initComponents();

            String caminho = new File("src/imagens/logo_ToPS_Visualization.png")
                    .getCanonicalPath();

            JImagePanel panel = new JImagePanel(caminho);
            panel.setSize(1360, 750);

            add(panel);
            pack();
            setTitle("ToPS Visualization");
            setExtendedState(MAXIMIZED_BOTH);
        } catch (IOException ex) {
            Logger.getLogger(ToPS_Visualization.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menu = new javax.swing.JMenuBar();
        menuNovo = new javax.swing.JMenu();
        menuNovoModelo = new javax.swing.JMenuItem();
        menuSair = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        menuNovo.setText("Novo");

        menuNovoModelo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        menuNovoModelo.setText("Modelo");
        menuNovoModelo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNovoModeloActionPerformed(evt);
            }
        });
        menuNovo.add(menuNovoModelo);

        menu.add(menuNovo);

        menuSair.setText("Sair");
        menuSair.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuSairMenuSelected(evt);
            }
        });
        menu.add(menuSair);

        setJMenuBar(menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 283, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuSairMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuSairMenuSelected

        int sair = JOptionPane.showConfirmDialog(rootPane, "Deseja realmente sair?",
                "ToPS Visualization", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (sair == JOptionPane.YES_OPTION) {
            System.exit(0);
        }

    }//GEN-LAST:event_menuSairMenuSelected

    private void menuNovoModeloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNovoModeloActionPerformed
       
        Modelo modelo = new Modelo();

    }//GEN-LAST:event_menuNovoModeloActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        ToPS_Visualization topsv = new ToPS_Visualization();

        topsv.setVisible(true);
        topsv.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar menu;
    private javax.swing.JMenu menuNovo;
    private javax.swing.JMenuItem menuNovoModelo;
    private javax.swing.JMenu menuSair;
    // End of variables declaration//GEN-END:variables
}