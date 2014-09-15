/*
 * Created on Feb 27, 2006 by pladd
 *
 */
package com.bottinifuel.AddressMapper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.bottinifuel.Energy.Info.AddressInfo;
import com.bottinifuel.Energy.Info.CustInfo;
import com.bottinifuel.Energy.Info.InfoFactory;

/**
 * @author pladd
 *
 */
public class AddressMapper extends JFrame
{
    private static InfoFactory EnergyInfo;
    
    private JPanel AddressPanel;
    private JTextField EnergyAcctNumber;
    private static final long serialVersionUID = 1L;

    public AddressMapper()
    {
        super("Bottini Address Mapper");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(412, 488);

        try
        {
            if (EnergyInfo == null)
                EnergyInfo = new InfoFactory();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "Unable to open Energy database connection\n" + e.getCause().toString(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        final JPanel LookupPanel = new JPanel();
        LookupPanel.setLayout(new BoxLayout(LookupPanel, BoxLayout.X_AXIS));
        getContentPane().add(LookupPanel, BorderLayout.NORTH);

        final JLabel energyLabel = new JLabel();
        energyLabel.setText("Acct #:");
        energyLabel.setDisplayedMnemonic('A');
        LookupPanel.add(energyLabel);

        EnergyAcctNumber = new JTextField();
        EnergyAcctNumber.setColumns(20);
        LookupPanel.add(EnergyAcctNumber);
        energyLabel.setLabelFor(EnergyAcctNumber);

        final JButton LookupBtn = new JButton("Lookup");
        LookupBtn.setMnemonic('L');
        LookupBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                RefreshAddresses();
            }
        });
        LookupPanel.add(LookupBtn);

        EnergyAcctNumber.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                LookupBtn.doClick();
            }
        });

        final JScrollPane AddressPane = new JScrollPane();
        getContentPane().add(AddressPane);

        AddressPanel = new JPanel();
        AddressPanel.setLayout(new GridBagLayout());
        AddressPane.setViewportView(AddressPanel);
    }

    private class MapButtonListener implements ActionListener
    {
        private final AddressInfo Address;
        public MapButtonListener(AddressInfo ai)
        {
            Address = ai;
        }

        public void actionPerformed(ActionEvent arg0)
        {
            try
            {
                String addr = Address.Street1 + " ";
                addr += "," + Address.City + " " + Address.State + " " + Address.Zip; 
                URI uri = new URI("http", "maps.google.com", "/maps", "q=" + addr, null);
                @SuppressWarnings("unused")
				Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + uri.toString());
            }
            catch (URISyntaxException e1)
            {
                String msg = "URL formatting error: " + e1.getMessage();
                JOptionPane.showMessageDialog(AddressPanel, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (IOException e)
            {
                String msg = "Error launching URL: " + e.getMessage();
                JOptionPane.showMessageDialog(AddressPanel, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int intToVK(int i)
    {
        switch (i)
        {
        case 1: return KeyEvent.VK_1;
        case 2: return KeyEvent.VK_2;
        case 3: return KeyEvent.VK_3;
        case 4: return KeyEvent.VK_4;
        case 5: return KeyEvent.VK_5;
        case 6: return KeyEvent.VK_6;
        case 7: return KeyEvent.VK_7;
        case 8: return KeyEvent.VK_8;
        case 9: return KeyEvent.VK_9;
        case 0: return KeyEvent.VK_0;
        default: return 0;
        }
    }
    
    private void PopulateAddressPanel(JPanel panel, CustInfo ci)
    {
        int i = 1;
        for (AddressInfo ai : ci.Addrs)
        {
            final JButton button = new JButton(i + ": " + ai.AddressLabel);
            button.setAlignmentX(Component.RIGHT_ALIGNMENT);
            button.setMnemonic(intToVK(i++));
            final GridBagConstraints buttonConstraints = new GridBagConstraints();
            buttonConstraints.anchor = GridBagConstraints.NORTHWEST;
            buttonConstraints.gridx = 0;
            buttonConstraints.gridy = -1;
            buttonConstraints.insets = new Insets(0, 0, 0, 15);
            panel.add(button, buttonConstraints);
            button.addActionListener(new MapButtonListener(ai));
            
            final JTextArea text = new JTextArea();
            text.setBackground(button.getBackground());
            text.setText(ai.toString());
            text.setEditable(false);
            final GridBagConstraints textConstraints = new GridBagConstraints();
            textConstraints.ipady = 5;
            textConstraints.ipadx = 5;
            textConstraints.gridx = 1;
            textConstraints.gridy = -1;
            panel.add(text, textConstraints);
        }
    }
    
    private void RefreshAddresses()
    {
        AddressPanel.removeAll();
        CustInfo ci = null;
        try
        {
            String a = EnergyAcctNumber.getText();
            int fullAccountNum = Integer.parseInt(a);
            int shortAcctNum = EnergyInfo.AccountNum(fullAccountNum);
            ci = EnergyInfo.GetCustomer(shortAcctNum);
        }
        catch (NumberFormatException e)
        {
        	try {
        		Vector<Integer> shortAccounts = EnergyInfo.SortCodeLookup(EnergyAcctNumber.getText());
        		if (shortAccounts == null)
        			throw new Exception("No account found for " + EnergyAcctNumber.getText());
        		if (shortAccounts.size() == 1)
        			ci = EnergyInfo.GetCustomer(shortAccounts.get(0).intValue());
        	}
        	catch (Exception e2)
        	{
        		AddressPanel.removeAll();
                JLabel errorLabel = new JLabel(e2.getMessage());
                AddressPanel.add(errorLabel);
        	}
        }
        catch (Exception e)
        {
            AddressPanel.removeAll();
            JLabel errorLabel = new JLabel(e.getMessage());
            AddressPanel.add(errorLabel);
        }

        if (ci != null)
        {
        	PopulateAddressPanel(AddressPanel, ci);
        }
        AddressPanel.validate();
        AddressPanel.repaint();
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        AddressMapper am = new AddressMapper();
        am.setVisible(true);
    }

}
