
/**
 * Cafeteria management application
 * Copyright (c) 2011, 2012 Paulo Silva
 * 
 * This file is part of Cafeteria.
 * 
 * Cafeteria is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Cafeteria is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cafeteria.  If not, see <http://www.gnu.org/licenses/>.
 */

package pt.uac.cafeteria.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import pt.uac.cafeteria.model.Application;
import pt.uac.cafeteria.model.ApplicationException;
import pt.uac.cafeteria.model.MapperRegistry;
import pt.uac.cafeteria.model.domain.Credit;
import pt.uac.cafeteria.model.domain.Day;
import pt.uac.cafeteria.model.domain.Meal;
import pt.uac.cafeteria.model.domain.Menu;
import pt.uac.cafeteria.model.domain.Student;
import pt.uac.cafeteria.model.domain.Ticket;
import pt.uac.cafeteria.model.domain.Transaction;
import pt.uac.cafeteria.model.validation.Validator;

/**
 * 
 * Represents the Front Office user interface.
 */
public class Frontend extends javax.swing.JFrame {

    private static final int LUNCH_HOUR = 13;
    private static final int DINNER_HOUR = 19;
    private static Day today = new Day();
    private Student student;
    private String currentPin;
    private String newPin;
    private String confirmPin;
    private String regex;

    /** Creates new form Frontend */
    public Frontend() {
        initComponents();
        
        Application.init();
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width/2 - 400, screenSize.height/2 - 300);
        
        ifLogInFailed.setVisible(false);
        menuPanel.setVisible(false);
    }

    private Day getSelectedDay() {

        int selYear = (Integer) cbYearChoice.getSelectedItem();
        int selMonth = cbMonthChoice.getSelectedIndex()+1;

        int selDay = cbDayChoice.getSelectedItem() != null
                  ? (Integer) cbDayChoice.getSelectedItem()
                  : cbDayChoice.getSelectedIndex();

        return new Day(
            selYear,
            selMonth,
            selDay
        );
    }

    private int monthTotalDays (int year, int month) {

        Calendar cal = new GregorianCalendar(year, month, 1);
        int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        return days;
    }

    private void updateDayItems() {
        cbDayChoice.removeAllItems();
        Integer selectedYear = (Integer) cbYearChoice.getSelectedItem();

        boolean thisMonth = cbMonthChoice.getSelectedIndex() + 1 == today.getMonth() && selectedYear.intValue() == today.getYear();
        int startingDay = thisMonth ? today.getDayOfMonth() : 1;

        for (int i = startingDay; i <= monthTotalDays(selectedYear, cbMonthChoice.getSelectedIndex()); i++) {
            cbDayChoice.addItem(i);
        }

        cbDayChoice.setSelectedIndex(0);

        checkMealTime();
        rbLunch.setSelected(false);
        rbDinner.setSelected(false);

        if (rbLunch.isEnabled() && rbLunch.isSelected() || rbDinner.isEnabled() && rbDinner.isSelected()) {
            panelShowMeal.setVisible(true);
        }
        else {
            bgTime.clearSelection();
            panelShowMeal.setVisible(false);
        }
    }

    private void checkMealTime() {
        int currentHour = today.getCalendar().get(Calendar.HOUR_OF_DAY);
        rbLunch.setEnabled(!(getSelectedDay().isToday() && currentHour > LUNCH_HOUR));
        rbDinner.setEnabled(!(getSelectedDay().isToday() && currentHour > DINNER_HOUR));
    }

    private void noMeal() {
        panelChooseDay.setEnabled(false);
        panelShowMeal.setEnabled(false);
        ifNoMeal.setVisible(true);
    }

    private void clearMealPanel() {
        lblMeatText.setText("");
        lblFishText.setText("");
        lblVegetarianText.setText("");

        rbMeat.setEnabled(false);
        rbFish.setEnabled(false);
        rbVegetarian.setEnabled(false);
    }

    private void updateMealChoices() {

        if (!rbLunch.isSelected() && !rbDinner.isSelected()) {
            return;
        }

        Day day = getSelectedDay();
        Menu menu = MapperRegistry.menu().find(day);

        if (menu == null) {
            noMeal();
            return;
        }

        Meal.Time mealTime = rbLunch.isSelected() ? Meal.Time.LUNCH : Meal.Time.DINNER;

        if (menu.isEmpty(mealTime)) {
            noMeal();
            return;
        }

        clearMealPanel();

        Map<Meal.Type, Meal> meals = menu.getMeals(mealTime);

        String soup = null;
        String dessert = null;

        for (Map.Entry<Meal.Type, Meal> meal : meals.entrySet()) {
            if (soup == null) {
                soup = meal.getValue().getSoup();
            }
            if (dessert == null) {
                dessert = meal.getValue().getDessert();
            }
            String mainCourse = meal.getValue().getMainCourse();

            switch(meal.getKey()) {
                case MEAT:
                    lblMeatText.setText(mainCourse);
                    rbMeat.setEnabled(true);
                    break;

                case FISH:
                    lblFishText.setText(mainCourse);
                    rbFish.setEnabled(true);
                    break;

                case VEGETARIAN:
                    lblVegetarianText.setText(mainCourse);
                    rbVegetarian.setEnabled(true);
                    break;
            }
        }

        lblSoupText.setText(soup);
        lblDessertText.setText(dessert);

        panelShowMeal.setVisible(true);
    }

    private Meal.Time getMealTimeChoice() {
        return rbLunch.isSelected() ? Meal.Time.LUNCH : Meal.Time.DINNER;
    }

    private Meal.Type getMealTypeChoice() {
        if (rbMeat.isSelected()) {
            return Meal.Type.MEAT;
        }
        if (rbFish.isSelected()) {
            return Meal.Type.FISH;
        }
        if (rbVegetarian.isSelected()) {
            return Meal.Type.VEGETARIAN;
        }

        return null;
    }

    private int changeStringToInt(String string) {
        try {
            int aux = Integer.parseInt(string);
            return aux;
        }
        catch (NumberFormatException e) {
            e.getMessage();
            return -1;
        }
    }
    
    private int ifIsNull(String number) {
        try {
            if (number.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(number);
        }
        catch (NumberFormatException e) {
            e.getMessage();
            return -1;
        }
    }
    
    /**
     * Method responsible of setting the component disabled
     * 
     * @param component The component that will be disable
     */
    private void deactivate(JComponent component) {
        component.setEnabled(false);
        for (int i = 0; i < component.getComponents().length; i++) {
            component.getComponent(i).setEnabled(false);
        }
    }
    
    /**
    * Method responsible of setting the component enabled
    * 
    * @param component The component that will be enable
    */
    private void activate(JComponent component) {
        component.setEnabled(false);
        for (int i = 0; i < component.getComponents().length; i++) {
            component.getComponent(i).setEnabled(true);
        }
    }
    
    /** Receives a transaction list and returns a JTable**/
    private String[] transactionRow(Transaction trans) {

        String transactionDay = new Day(trans.getDate()).format("yyyy-MM-dd HH:mm:ss");
        String amount = String.format("€%.2f", trans.getAmount());

        if (trans instanceof Credit) {
            Credit credit = (Credit) trans;
            return new String[] {
                "Carregamento", transactionDay, amount, credit.getAdministrator()
            };
        }
        if (trans instanceof Ticket) {
            Meal meal = ((Ticket) trans).getMeal();
            return new String[] {
                "Compra de senha", transactionDay, amount, meal.getDay() + ", " + meal.getTime()
            };
        }   
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgTime = new javax.swing.ButtonGroup();
        bgDish = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        ifLogInFailed = new javax.swing.JInternalFrame();
        lblLogInFailed = new javax.swing.JLabel();
        btnLogInFailed = new javax.swing.JButton();
        lblNumber = new javax.swing.JLabel();
        tfNumber = new javax.swing.JTextField();
        lblPinCode = new javax.swing.JLabel();
        pfPinCode = new javax.swing.JPasswordField();
        btnConfirm = new javax.swing.JButton();
        lblLogo = new javax.swing.JLabel();
        lblFrontBK = new javax.swing.JLabel();
        panelLogin = new javax.swing.JPanel();
        menuPanel = new javax.swing.JPanel();
        ifLogOut = new javax.swing.JInternalFrame();
        lblLogOut1 = new javax.swing.JLabel();
        btnYesCancelLogOut = new javax.swing.JButton();
        btnNoCancelLogOut = new javax.swing.JButton();
        panelButtons = new javax.swing.JPanel();
        btnBuyTicket = new javax.swing.JButton();
        btnCheckBalance = new javax.swing.JButton();
        btnChangePinCode = new javax.swing.JButton();
        btnChangeEmail = new javax.swing.JButton();
        btnLogOut = new javax.swing.JButton();
        panelWelcome = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        lblWelcome1 = new javax.swing.JLabel();
        lblWelcome2 = new javax.swing.JLabel();
        lblWelcome3 = new javax.swing.JLabel();
        panelBuyTicket = new javax.swing.JPanel();
        ifNoMeal = new javax.swing.JInternalFrame();
        lblNoMeal = new javax.swing.JLabel();
        btnNoMeal = new javax.swing.JButton();
        ifCancelBuyTicket = new javax.swing.JInternalFrame();
        lblCancelBuyTickets = new javax.swing.JLabel();
        btnYesCancel = new javax.swing.JButton();
        btnNoCancel = new javax.swing.JButton();
        ifMoreTickets = new javax.swing.JInternalFrame();
        lblMoreTickets2 = new javax.swing.JLabel();
        btnYesTickets = new javax.swing.JButton();
        btnNoTickets = new javax.swing.JButton();
        ifPurchaseSuccess = new javax.swing.JInternalFrame();
        lblPurchaseSuccess = new javax.swing.JLabel();
        btnPurchaseOk = new javax.swing.JButton();
        panelSummary = new javax.swing.JPanel();
        lblPurchaseDate = new javax.swing.JLabel();
        lblPurchaseDateText = new javax.swing.JLabel();
        lblPrice = new javax.swing.JLabel();
        lblPriceText = new javax.swing.JLabel();
        lpMeal1 = new javax.swing.JLayeredPane();
        lblTicketMealDate = new javax.swing.JLabel();
        lblTicketMealTime = new javax.swing.JLabel();
        lblTicketSoup = new javax.swing.JLabel();
        lblTicketDish = new javax.swing.JLabel();
        lblTicketDessert = new javax.swing.JLabel();
        lblTicketMealDateText = new javax.swing.JLabel();
        lbTicketlMealTimeText = new javax.swing.JLabel();
        lblTicketSoupText = new javax.swing.JLabel();
        lblTicketDishText = new javax.swing.JLabel();
        lblTicketDessertText = new javax.swing.JLabel();
        panelChooseDay = new javax.swing.JPanel();
        cbYearChoice = new javax.swing.JComboBox();
        cbMonthChoice = new javax.swing.JComboBox();
        cbDayChoice = new javax.swing.JComboBox();
        rbLunch = new javax.swing.JRadioButton();
        rbDinner = new javax.swing.JRadioButton();
        panelShowMeal = new javax.swing.JPanel();
        lblChooseDish = new javax.swing.JLabel();
        rbMeat = new javax.swing.JRadioButton();
        rbFish = new javax.swing.JRadioButton();
        rbVegetarian = new javax.swing.JRadioButton();
        lblSoup = new javax.swing.JLabel();
        lblMeat = new javax.swing.JLabel();
        lblFish = new javax.swing.JLabel();
        lblVegetarian = new javax.swing.JLabel();
        lblDessert = new javax.swing.JLabel();
        lblSoupText = new javax.swing.JLabel();
        lblMeatText = new javax.swing.JLabel();
        lblFishText = new javax.swing.JLabel();
        lblVegetarianText = new javax.swing.JLabel();
        lblDessertText = new javax.swing.JLabel();
        btnBuy = new javax.swing.JButton();
        btnConfirmMeal = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        panelCheckBalance = new javax.swing.JPanel();
        panelStudent = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        lblAddress = new javax.swing.JLabel();
        lblPhone = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        lblScholarship = new javax.swing.JLabel();
        lblCourse = new javax.swing.JLabel();
        lblNameText = new javax.swing.JLabel();
        lblAddressText = new javax.swing.JLabel();
        lblPhoneText = new javax.swing.JLabel();
        lblEmailText = new javax.swing.JLabel();
        chbScholarship = new javax.swing.JCheckBox();
        lblCourseText = new javax.swing.JLabel();
        panelAccount = new javax.swing.JPanel();
        lblAccount = new javax.swing.JLabel();
        lblAccountText = new javax.swing.JLabel();
        lblBalance = new javax.swing.JLabel();
        lblBalanceText = new javax.swing.JLabel();
        spTransactions = new javax.swing.JScrollPane();
        tableTransactions = new javax.swing.JTable();
        panelChangePinCode = new javax.swing.JPanel();
        ifCancelPinCode = new javax.swing.JInternalFrame();
        lblCancelPinCode = new javax.swing.JLabel();
        btnYesCancelPinCode = new javax.swing.JButton();
        btnNoCancelPinCode = new javax.swing.JButton();
        ifChangePinCodeSuccess = new javax.swing.JInternalFrame();
        lblChangePinCodeSuccess1 = new javax.swing.JLabel();
        lblChangePinCodeSuccess2 = new javax.swing.JLabel();
        btnChangePinCodeSuccessOk = new javax.swing.JButton();
        panelChangePinCodeFields = new javax.swing.JPanel();
        lblCurrentPin = new javax.swing.JLabel();
        lblNewPin = new javax.swing.JLabel();
        lblConfiirmPin = new javax.swing.JLabel();
        pfCurrentPin = new javax.swing.JPasswordField();
        pfNewPin = new javax.swing.JPasswordField();
        pfConfirmPin = new javax.swing.JPasswordField();
        btnValidatePinCode = new javax.swing.JButton();
        btnCancelNewPinCode = new javax.swing.JButton();
        panelChangeEmail = new javax.swing.JPanel();
        ifCancelEmail = new javax.swing.JInternalFrame();
        lblCancelEmail = new javax.swing.JLabel();
        btnYesCancelEmail = new javax.swing.JButton();
        btnNoCancelEmail = new javax.swing.JButton();
        ifChangeEmailSuccess = new javax.swing.JInternalFrame();
        lblChangeEmailSuccess1 = new javax.swing.JLabel();
        lblChangeEmailSuccess2 = new javax.swing.JLabel();
        btnChangeEmailOk = new javax.swing.JButton();
        panelChangeEmailFields = new javax.swing.JPanel();
        lblCurrentEmail = new javax.swing.JLabel();
        lblNewEmail = new javax.swing.JLabel();
        lblCurrentEmailText = new javax.swing.JLabel();
        tfNewMailText = new javax.swing.JTextField();
        btnValidateEmail = new javax.swing.JButton();
        btnCancelNewEmail = new javax.swing.JButton();
        lblLogo1 = new javax.swing.JLabel();
        lblFrontBK1 = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cafeteria");
        setMinimumSize(new java.awt.Dimension(800, 600));
        setResizable(false);

        mainPanel.setMinimumSize(new java.awt.Dimension(800, 600));
        mainPanel.setLayout(null);

        ifLogInFailed.setTitle("Informação");
        ifLogInFailed.setVisible(true);

        lblLogInFailed.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblLogInFailed.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogInFailed.setText("Dados Inválidos! Tente Novamente");

        btnLogInFailed.setText("OK");
        btnLogInFailed.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnLogInFailedMouseReleased(evt);
            }
        });
        btnLogInFailed.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                btnLogInFailedKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout ifLogInFailedLayout = new javax.swing.GroupLayout(ifLogInFailed.getContentPane());
        ifLogInFailed.getContentPane().setLayout(ifLogInFailedLayout);
        ifLogInFailedLayout.setHorizontalGroup(
            ifLogInFailedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblLogInFailed, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
            .addGroup(ifLogInFailedLayout.createSequentialGroup()
                .addGap(92, 92, 92)
                .addComponent(btnLogInFailed, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
        );
        ifLogInFailedLayout.setVerticalGroup(
            ifLogInFailedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifLogInFailedLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblLogInFailed, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLogInFailed)
                .addContainerGap(55, Short.MAX_VALUE))
        );

        mainPanel.add(ifLogInFailed);
        ifLogInFailed.setBounds(270, 160, 258, 172);

        lblNumber.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblNumber.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNumber.setText("Número de Conta");
        mainPanel.add(lblNumber);
        lblNumber.setBounds(305, 150, 190, 14);

        tfNumber.setFont(new java.awt.Font("Tahoma", 1, 11));
        tfNumber.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        mainPanel.add(tfNumber);
        tfNumber.setBounds(345, 170, 110, 30);

        lblPinCode.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblPinCode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPinCode.setText("Código de Acesso");
        mainPanel.add(lblPinCode);
        lblPinCode.setBounds(305, 240, 190, 14);

        pfPinCode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                pfPinCodeKeyReleased(evt);
            }
        });
        mainPanel.add(pfPinCode);
        pfPinCode.setBounds(345, 265, 110, 30);

        btnConfirm.setText("Confirmar");
        btnConfirm.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnConfirmMouseReleased(evt);
            }
        });
        mainPanel.add(btnConfirm);
        btnConfirm.setBounds(355, 343, 90, 30);

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pt/uac/cafeteria/ui/images/logo.png"))); // NOI18N
        mainPanel.add(lblLogo);
        lblLogo.setBounds(350, 390, 100, 100);

        lblFrontBK.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pt/uac/cafeteria/ui/images/frontBK.png"))); // NOI18N
        mainPanel.add(lblFrontBK);
        lblFrontBK.setBounds(0, 0, 939, 683);

        panelLogin.setBackground(new java.awt.Color(255, 255, 255));
        panelLogin.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout panelLoginLayout = new javax.swing.GroupLayout(panelLogin);
        panelLogin.setLayout(panelLoginLayout);
        panelLoginLayout.setHorizontalGroup(
            panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 196, Short.MAX_VALUE)
        );
        panelLoginLayout.setVerticalGroup(
            panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 396, Short.MAX_VALUE)
        );

        mainPanel.add(panelLogin);
        panelLogin.setBounds(300, 100, 200, 400);

        menuPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        menuPanel.setLayout(null);

        ifLogOut.setTitle("Aviso");
        ifLogOut.setVisible(true);

        lblLogOut1.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblLogOut1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogOut1.setText("Tem a certeza que quer terminar?");

        btnYesCancelLogOut.setText("Sim");
        btnYesCancelLogOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnYesCancelLogOutMouseReleased(evt);
            }
        });

        btnNoCancelLogOut.setText("Não");
        btnNoCancelLogOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnNoCancelLogOutMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout ifLogOutLayout = new javax.swing.GroupLayout(ifLogOut.getContentPane());
        ifLogOut.getContentPane().setLayout(ifLogOutLayout);
        ifLogOutLayout.setHorizontalGroup(
            ifLogOutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifLogOutLayout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(btnYesCancelLogOut)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNoCancelLogOut)
                .addContainerGap())
            .addGroup(ifLogOutLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(lblLogOut1, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
        );
        ifLogOutLayout.setVerticalGroup(
            ifLogOutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifLogOutLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(lblLogOut1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(ifLogOutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNoCancelLogOut)
                    .addComponent(btnYesCancelLogOut))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        menuPanel.add(ifLogOut);
        ifLogOut.setBounds(360, 200, 220, 160);

        panelButtons.setOpaque(false);
        panelButtons.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnBuyTicket.setText("Comprar Senha");
        btnBuyTicket.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnBuyTicketMouseReleased(evt);
            }
        });
        panelButtons.add(btnBuyTicket, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 32, 183, 30));

        btnCheckBalance.setText("Consultar Saldo");
        btnCheckBalance.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnCheckBalanceMouseReleased(evt);
            }
        });
        panelButtons.add(btnCheckBalance, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 81, 183, 30));

        btnChangePinCode.setText("Alterar Código de Acesso");
        btnChangePinCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnChangePinCodeMouseReleased(evt);
            }
        });
        panelButtons.add(btnChangePinCode, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 131, 183, 30));

        btnChangeEmail.setText("Alterar e-mail");
        btnChangeEmail.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnChangeEmailMouseReleased(evt);
            }
        });
        panelButtons.add(btnChangeEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 179, 183, 30));

        btnLogOut.setText("Terminar");
        btnLogOut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnLogOutMouseReleased(evt);
            }
        });
        panelButtons.add(btnLogOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 516, 183, 30));

        menuPanel.add(panelButtons);
        panelButtons.setBounds(0, 0, 200, 600);

        panelWelcome.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Bem-Vindo", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP));

        lblWelcome.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblWelcome.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblWelcome.setText("Aplicação para a compra de senhas da");

        lblWelcome1.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblWelcome1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblWelcome1.setText("Universidade dos Açores");

        lblWelcome2.setText("- Escolha uma das operações disponíveis.");

        lblWelcome3.setText("- Quando concluir as suas operações, pressione o botão Terminar.");

        javax.swing.GroupLayout panelWelcomeLayout = new javax.swing.GroupLayout(panelWelcome);
        panelWelcome.setLayout(panelWelcomeLayout);
        panelWelcomeLayout.setHorizontalGroup(
            panelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWelcomeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWelcome1, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                    .addComponent(lblWelcome, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                    .addComponent(lblWelcome2)
                    .addComponent(lblWelcome3))
                .addContainerGap())
        );
        panelWelcomeLayout.setVerticalGroup(
            panelWelcomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelWelcomeLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblWelcome)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblWelcome1)
                .addGap(33, 33, 33)
                .addComponent(lblWelcome2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblWelcome3)
                .addContainerGap(53, Short.MAX_VALUE))
        );

        menuPanel.add(panelWelcome);
        panelWelcome.setBounds(250, 25, 440, 210);

        panelBuyTicket.setOpaque(false);
        panelBuyTicket.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ifNoMeal.setTitle("Informação");
        ifNoMeal.setVisible(true);

        lblNoMeal.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblNoMeal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNoMeal.setText("Não existe refeição disponível");

        btnNoMeal.setText("OK");
        btnNoMeal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnNoMealMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout ifNoMealLayout = new javax.swing.GroupLayout(ifNoMeal.getContentPane());
        ifNoMeal.getContentPane().setLayout(ifNoMealLayout);
        ifNoMealLayout.setHorizontalGroup(
            ifNoMealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblNoMeal, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
            .addGroup(ifNoMealLayout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(btnNoMeal, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(76, Short.MAX_VALUE))
        );
        ifNoMealLayout.setVerticalGroup(
            ifNoMealLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifNoMealLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblNoMeal, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNoMeal)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        panelBuyTicket.add(ifNoMeal, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, 220, 160));

        ifCancelBuyTicket.setTitle("Aviso");
        ifCancelBuyTicket.setVisible(true);

        lblCancelBuyTickets.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblCancelBuyTickets.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCancelBuyTickets.setText("Tem a certeza que deseja Cancelar?");

        btnYesCancel.setText("Sim");
        btnYesCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnYesCancelMouseReleased(evt);
            }
        });

        btnNoCancel.setText("Não");
        btnNoCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnNoCancelMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout ifCancelBuyTicketLayout = new javax.swing.GroupLayout(ifCancelBuyTicket.getContentPane());
        ifCancelBuyTicket.getContentPane().setLayout(ifCancelBuyTicketLayout);
        ifCancelBuyTicketLayout.setHorizontalGroup(
            ifCancelBuyTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ifCancelBuyTicketLayout.createSequentialGroup()
                .addContainerGap(51, Short.MAX_VALUE)
                .addComponent(btnYesCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNoCancel)
                .addGap(43, 43, 43))
            .addComponent(lblCancelBuyTickets, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ifCancelBuyTicketLayout.setVerticalGroup(
            ifCancelBuyTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifCancelBuyTicketLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblCancelBuyTickets, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(ifCancelBuyTicketLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNoCancel)
                    .addComponent(btnYesCancel))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        panelBuyTicket.add(ifCancelBuyTicket, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, 220, 160));

        ifMoreTickets.setTitle("Informação");
        ifMoreTickets.setVisible(true);

        lblMoreTickets2.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblMoreTickets2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMoreTickets2.setText("Deseja adquirir mais senhas?");

        btnYesTickets.setText("Sim");
        btnYesTickets.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnYesTicketsMouseReleased(evt);
            }
        });

        btnNoTickets.setText("Não");
        btnNoTickets.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnNoTicketsMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout ifMoreTicketsLayout = new javax.swing.GroupLayout(ifMoreTickets.getContentPane());
        ifMoreTickets.getContentPane().setLayout(ifMoreTicketsLayout);
        ifMoreTicketsLayout.setHorizontalGroup(
            ifMoreTicketsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifMoreTicketsLayout.createSequentialGroup()
                .addContainerGap(49, Short.MAX_VALUE)
                .addComponent(btnYesTickets)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNoTickets)
                .addGap(45, 45, 45))
            .addComponent(lblMoreTickets2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
        );
        ifMoreTicketsLayout.setVerticalGroup(
            ifMoreTicketsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ifMoreTicketsLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblMoreTickets2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addGroup(ifMoreTicketsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNoTickets)
                    .addComponent(btnYesTickets))
                .addGap(20, 20, 20))
        );

        panelBuyTicket.add(ifMoreTickets, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, 220, 160));

        ifPurchaseSuccess.setTitle("Informação");
        ifPurchaseSuccess.setVisible(true);

        lblPurchaseSuccess.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblPurchaseSuccess.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPurchaseSuccess.setText("Compra efectuada com sucesso!");

        btnPurchaseOk.setText("OK");
        btnPurchaseOk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnPurchaseOkMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout ifPurchaseSuccessLayout = new javax.swing.GroupLayout(ifPurchaseSuccess.getContentPane());
        ifPurchaseSuccess.getContentPane().setLayout(ifPurchaseSuccessLayout);
        ifPurchaseSuccessLayout.setHorizontalGroup(
            ifPurchaseSuccessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblPurchaseSuccess, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
            .addGroup(ifPurchaseSuccessLayout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(btnPurchaseOk, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(76, Short.MAX_VALUE))
        );
        ifPurchaseSuccessLayout.setVerticalGroup(
            ifPurchaseSuccessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifPurchaseSuccessLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblPurchaseSuccess, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnPurchaseOk)
                .addContainerGap(43, Short.MAX_VALUE))
        );

        panelBuyTicket.add(ifPurchaseSuccess, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, 220, 160));

        panelSummary.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Resumo", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP));
        panelSummary.setPreferredSize(new java.awt.Dimension(500, 270));
        panelSummary.setLayout(null);

        lblPurchaseDate.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblPurchaseDate.setText("Data da Compra:");
        panelSummary.add(lblPurchaseDate);
        lblPurchaseDate.setBounds(16, 31, 94, 23);
        panelSummary.add(lblPurchaseDateText);
        lblPurchaseDateText.setBounds(120, 31, 108, 23);

        lblPrice.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblPrice.setText("Preço Total:");
        panelSummary.add(lblPrice);
        lblPrice.setBounds(16, 60, 67, 23);

        lblPriceText.setText("3€");
        panelSummary.add(lblPriceText);
        lblPriceText.setBounds(93, 62, 63, 18);

        lpMeal1.setBackground(new java.awt.Color(102, 102, 102));
        lpMeal1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        lblTicketMealDate.setText("Data:");
        lblTicketMealDate.setBounds(10, 10, 50, 20);
        lpMeal1.add(lblTicketMealDate, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblTicketMealTime.setText("Refeição");
        lblTicketMealTime.setBounds(10, 40, 70, 20);
        lpMeal1.add(lblTicketMealTime, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblTicketSoup.setText("Sopa:");
        lblTicketSoup.setBounds(10, 70, 60, 20);
        lpMeal1.add(lblTicketSoup, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblTicketDish.setText("Carne:");
        lblTicketDish.setBounds(10, 100, 70, 20);
        lpMeal1.add(lblTicketDish, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblTicketDessert.setText("Sobremesa:");
        lblTicketDessert.setBounds(10, 130, 80, 20);
        lpMeal1.add(lblTicketDessert, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblTicketMealDateText.setText("10/1/2012");
        lblTicketMealDateText.setBounds(100, 10, 70, 20);
        lpMeal1.add(lblTicketMealDateText, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lbTicketlMealTimeText.setText("Almoço");
        lbTicketlMealTimeText.setBounds(100, 40, 170, 20);
        lpMeal1.add(lbTicketlMealTimeText, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblTicketSoupText.setText("Caldo Verde");
        lblTicketSoupText.setBounds(100, 70, 300, 20);
        lpMeal1.add(lblTicketSoupText, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblTicketDishText.setText("Bifes");
        lblTicketDishText.setBounds(100, 100, 300, 20);
        lpMeal1.add(lblTicketDishText, javax.swing.JLayeredPane.DEFAULT_LAYER);

        lblTicketDessertText.setText("Mousse");
        lblTicketDessertText.setBounds(100, 130, 300, 20);
        lpMeal1.add(lblTicketDessertText, javax.swing.JLayeredPane.DEFAULT_LAYER);

        panelSummary.add(lpMeal1);
        lpMeal1.setBounds(16, 101, 470, 150);

        panelBuyTicket.add(panelSummary, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        panelChooseDay.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Escolha a data e a refeição", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP));
        panelChooseDay.setMinimumSize(new java.awt.Dimension(400, 400));
        panelChooseDay.setPreferredSize(new java.awt.Dimension(333, 155));
        panelChooseDay.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        cbYearChoice.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbYearChoiceItemStateChanged(evt);
            }
        });
        panelChooseDay.add(cbYearChoice, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, 70, -1));

        cbMonthChoice.setMaximumRowCount(12);
        cbMonthChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" }));
        cbMonthChoice.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbMonthChoiceItemStateChanged(evt);
            }
        });
        panelChooseDay.add(cbMonthChoice, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 40, 90, -1));

        cbDayChoice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        cbDayChoice.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbDayChoiceItemStateChanged(evt);
            }
        });
        panelChooseDay.add(cbDayChoice, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 40, -1, -1));

        bgTime.add(rbLunch);
        rbLunch.setText("Almoço");
        rbLunch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rbLunchMouseReleased(evt);
            }
        });
        panelChooseDay.add(rbLunch, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 97, -1, -1));

        bgTime.add(rbDinner);
        rbDinner.setText("Jantar");
        rbDinner.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rbDinnerMouseReleased(evt);
            }
        });
        panelChooseDay.add(rbDinner, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 123, -1, -1));

        panelBuyTicket.add(panelChooseDay, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 440, -1));

        panelShowMeal.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "MENU", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP));
        panelShowMeal.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblChooseDish.setText("Faça a sua escolha:");
        panelShowMeal.add(lblChooseDish, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, -1, -1));

        bgDish.add(rbMeat);
        rbMeat.setText("Carne");
        rbMeat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rbMeatMouseReleased(evt);
            }
        });
        panelShowMeal.add(rbMeat, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 220, -1, -1));

        bgDish.add(rbFish);
        rbFish.setText("Peixe");
        rbFish.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rbFishMouseReleased(evt);
            }
        });
        panelShowMeal.add(rbFish, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 220, -1, -1));

        bgDish.add(rbVegetarian);
        rbVegetarian.setText("Vegetariano");
        rbVegetarian.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rbVegetarianMouseReleased(evt);
            }
        });
        panelShowMeal.add(rbVegetarian, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 220, -1, -1));

        lblSoup.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblSoup.setText("Sopa:");
        panelShowMeal.add(lblSoup, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, 20));

        lblMeat.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblMeat.setText("Carne:");
        panelShowMeal.add(lblMeat, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, -1, 20));

        lblFish.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblFish.setText("Peixe:");
        panelShowMeal.add(lblFish, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, 20));

        lblVegetarian.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblVegetarian.setText("Vegetariano:");
        panelShowMeal.add(lblVegetarian, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, -1, 20));

        lblDessert.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblDessert.setText("Sobremesa:");
        panelShowMeal.add(lblDessert, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 160, -1, 20));
        panelShowMeal.add(lblSoupText, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 40, 290, 20));
        panelShowMeal.add(lblMeatText, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 290, 20));
        panelShowMeal.add(lblFishText, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 100, 290, 20));
        panelShowMeal.add(lblVegetarianText, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 130, 290, 20));
        panelShowMeal.add(lblDessertText, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 160, 290, 20));

        panelBuyTicket.add(panelShowMeal, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 170, 440, 260));

        btnBuy.setText("Comprar");
        btnBuy.setPreferredSize(new java.awt.Dimension(75, 23));
        btnBuy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnBuyMouseReleased(evt);
            }
        });
        panelBuyTicket.add(btnBuy, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 490, 80, 30));

        btnConfirmMeal.setText("OK");
        btnConfirmMeal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnConfirmMealMouseReleased(evt);
            }
        });
        panelBuyTicket.add(btnConfirmMeal, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 490, 80, 30));

        btnCancel.setText("Cancelar");
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnCancelMouseReleased(evt);
            }
        });
        panelBuyTicket.add(btnCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 490, 80, 30));

        menuPanel.add(panelBuyTicket);
        panelBuyTicket.setBounds(250, 25, 500, 520);

        panelCheckBalance.setOpaque(false);
        panelCheckBalance.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        panelStudent.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Aluno", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP));

        lblName.setText("Nome:");

        lblAddress.setText("Rua:");

        lblPhone.setText("Telefone:");

        lblEmail.setText("Email:");

        lblScholarship.setText("Bolseiro:");

        lblCourse.setText("Curso:");

        chbScholarship.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        chbScholarship.setEnabled(false);

        javax.swing.GroupLayout panelStudentLayout = new javax.swing.GroupLayout(panelStudent);
        panelStudent.setLayout(panelStudentLayout);
        panelStudentLayout.setHorizontalGroup(
            panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelStudentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblName, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblAddress, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPhone, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEmail, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblScholarship, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCourse, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(21, 21, 21)
                .addGroup(panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chbScholarship)
                    .addComponent(lblNameText, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                    .addComponent(lblAddressText, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                    .addComponent(lblPhoneText, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEmailText, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                    .addComponent(lblCourseText, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelStudentLayout.setVerticalGroup(
            panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelStudentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNameText, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAddressText, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPhoneText, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEmailText, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblScholarship, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chbScholarship))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelStudentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCourse, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCourseText, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelCheckBalance.add(panelStudent, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 440, 210));

        panelAccount.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Movimentos de Conta", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP));
        panelAccount.setLayout(null);

        lblAccount.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblAccount.setText("Conta: ");
        panelAccount.add(lblAccount);
        lblAccount.setBounds(16, 31, 39, 14);

        lblAccountText.setText("               ");
        panelAccount.add(lblAccountText);
        lblAccountText.setBounds(61, 31, 60, 14);

        lblBalance.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblBalance.setText("Saldo:");
        panelAccount.add(lblBalance);
        lblBalance.setBounds(235, 31, 34, 14);

        lblBalanceText.setText("        ");
        panelAccount.add(lblBalanceText);
        lblBalanceText.setBounds(279, 31, 90, 14);

        tableTransactions.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tipo", "Data", "Montante", "Detalhes"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableTransactions.getTableHeader().setReorderingAllowed(false);
        spTransactions.setViewportView(tableTransactions);
        tableTransactions.getColumnModel().getColumn(0).setMinWidth(110);
        tableTransactions.getColumnModel().getColumn(0).setPreferredWidth(110);
        tableTransactions.getColumnModel().getColumn(0).setMaxWidth(110);
        tableTransactions.getColumnModel().getColumn(1).setMinWidth(130);
        tableTransactions.getColumnModel().getColumn(1).setPreferredWidth(130);
        tableTransactions.getColumnModel().getColumn(1).setMaxWidth(130);
        tableTransactions.getColumnModel().getColumn(2).setMinWidth(70);
        tableTransactions.getColumnModel().getColumn(2).setPreferredWidth(70);
        tableTransactions.getColumnModel().getColumn(2).setMaxWidth(70);
        tableTransactions.getColumnModel().getColumn(3).setMinWidth(115);
        tableTransactions.getColumnModel().getColumn(3).setPreferredWidth(115);
        tableTransactions.getColumnModel().getColumn(3).setMaxWidth(115);

        panelAccount.add(spTransactions);
        spTransactions.setBounds(16, 51, 408, 221);

        panelCheckBalance.add(panelAccount, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 230, 440, 290));

        menuPanel.add(panelCheckBalance);
        panelCheckBalance.setBounds(250, 25, 440, 522);

        panelChangePinCode.setMinimumSize(new java.awt.Dimension(440, 520));
        panelChangePinCode.setOpaque(false);
        panelChangePinCode.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ifCancelPinCode.setTitle("Aviso");
        ifCancelPinCode.setVisible(true);

        lblCancelPinCode.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblCancelPinCode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCancelPinCode.setText("Tem a certeza que deseja Cancelar?");

        btnYesCancelPinCode.setText("Sim");
        btnYesCancelPinCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnYesCancelPinCodeMouseReleased(evt);
            }
        });

        btnNoCancelPinCode.setText("Não");
        btnNoCancelPinCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnNoCancelPinCodeMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout ifCancelPinCodeLayout = new javax.swing.GroupLayout(ifCancelPinCode.getContentPane());
        ifCancelPinCode.getContentPane().setLayout(ifCancelPinCodeLayout);
        ifCancelPinCodeLayout.setHorizontalGroup(
            ifCancelPinCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ifCancelPinCodeLayout.createSequentialGroup()
                .addContainerGap(51, Short.MAX_VALUE)
                .addComponent(btnYesCancelPinCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNoCancelPinCode)
                .addGap(43, 43, 43))
            .addComponent(lblCancelPinCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ifCancelPinCodeLayout.setVerticalGroup(
            ifCancelPinCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifCancelPinCodeLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblCancelPinCode, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(ifCancelPinCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNoCancelPinCode)
                    .addComponent(btnYesCancelPinCode))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        panelChangePinCode.add(ifCancelPinCode, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, 220, 160));

        ifChangePinCodeSuccess.setTitle("Informação");
        ifChangePinCodeSuccess.setVisible(true);

        lblChangePinCodeSuccess1.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblChangePinCodeSuccess1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblChangePinCodeSuccess1.setText("Código de acesso alterado com sucesso!");

        lblChangePinCodeSuccess2.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblChangePinCodeSuccess2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblChangePinCodeSuccess2.setText("Consulte a sua caixa de correio.");

        btnChangePinCodeSuccessOk.setText("OK");
        btnChangePinCodeSuccessOk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnChangePinCodeSuccessOkMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout ifChangePinCodeSuccessLayout = new javax.swing.GroupLayout(ifChangePinCodeSuccess.getContentPane());
        ifChangePinCodeSuccess.getContentPane().setLayout(ifChangePinCodeSuccessLayout);
        ifChangePinCodeSuccessLayout.setHorizontalGroup(
            ifChangePinCodeSuccessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblChangePinCodeSuccess1, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
            .addComponent(lblChangePinCodeSuccess2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
            .addGroup(ifChangePinCodeSuccessLayout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addComponent(btnChangePinCodeSuccessOk, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
        );
        ifChangePinCodeSuccessLayout.setVerticalGroup(
            ifChangePinCodeSuccessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifChangePinCodeSuccessLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(lblChangePinCodeSuccess1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblChangePinCodeSuccess2)
                .addGap(18, 18, 18)
                .addComponent(btnChangePinCodeSuccessOk)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        panelChangePinCode.add(ifChangePinCodeSuccess, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 200, 260, 160));

        panelChangePinCodeFields.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Alterar Código de Acesso", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP));

        lblCurrentPin.setText("Insira o código corrente:");

        lblNewPin.setText("Insira o novo código:");

        lblConfiirmPin.setText("Confirmar novo código:");

        javax.swing.GroupLayout panelChangePinCodeFieldsLayout = new javax.swing.GroupLayout(panelChangePinCodeFields);
        panelChangePinCodeFields.setLayout(panelChangePinCodeFieldsLayout);
        panelChangePinCodeFieldsLayout.setHorizontalGroup(
            panelChangePinCodeFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelChangePinCodeFieldsLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panelChangePinCodeFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblConfiirmPin)
                    .addComponent(lblNewPin)
                    .addComponent(lblCurrentPin))
                .addGap(18, 18, 18)
                .addGroup(panelChangePinCodeFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pfCurrentPin, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pfNewPin, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pfConfirmPin, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(215, Short.MAX_VALUE))
        );
        panelChangePinCodeFieldsLayout.setVerticalGroup(
            panelChangePinCodeFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelChangePinCodeFieldsLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(panelChangePinCodeFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pfCurrentPin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelChangePinCodeFieldsLayout.createSequentialGroup()
                        .addComponent(lblCurrentPin, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addGroup(panelChangePinCodeFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNewPin, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pfNewPin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(6, 6, 6)
                        .addGroup(panelChangePinCodeFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblConfiirmPin, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pfConfirmPin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        panelChangePinCode.add(panelChangePinCodeFields, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 440, 210));

        btnValidatePinCode.setText("Alterar");
        btnValidatePinCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnValidatePinCodeMouseReleased(evt);
            }
        });
        panelChangePinCode.add(btnValidatePinCode, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 490, 80, 30));

        btnCancelNewPinCode.setText("Cancelar");
        btnCancelNewPinCode.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnCancelNewPinCodeMouseReleased(evt);
            }
        });
        panelChangePinCode.add(btnCancelNewPinCode, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 490, 80, 30));

        menuPanel.add(panelChangePinCode);
        panelChangePinCode.setBounds(250, 25, 440, 522);

        panelChangeEmail.setOpaque(false);
        panelChangeEmail.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ifCancelEmail.setTitle("Aviso");
        ifCancelEmail.setVisible(true);

        lblCancelEmail.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblCancelEmail.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCancelEmail.setText("Tem a certeza que deseja Cancelar?");

        btnYesCancelEmail.setText("Sim");
        btnYesCancelEmail.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnYesCancelEmailMouseReleased(evt);
            }
        });

        btnNoCancelEmail.setText("Não");
        btnNoCancelEmail.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnNoCancelEmailMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout ifCancelEmailLayout = new javax.swing.GroupLayout(ifCancelEmail.getContentPane());
        ifCancelEmail.getContentPane().setLayout(ifCancelEmailLayout);
        ifCancelEmailLayout.setHorizontalGroup(
            ifCancelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ifCancelEmailLayout.createSequentialGroup()
                .addContainerGap(51, Short.MAX_VALUE)
                .addComponent(btnYesCancelEmail)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnNoCancelEmail)
                .addGap(43, 43, 43))
            .addComponent(lblCancelEmail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        ifCancelEmailLayout.setVerticalGroup(
            ifCancelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifCancelEmailLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblCancelEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(ifCancelEmailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnNoCancelEmail)
                    .addComponent(btnYesCancelEmail))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        panelChangeEmail.add(ifCancelEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 150, 220, 160));

        ifChangeEmailSuccess.setTitle("Informação");
        ifChangeEmailSuccess.setVisible(true);

        lblChangeEmailSuccess1.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblChangeEmailSuccess1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblChangeEmailSuccess1.setText("Email alterado com sucesso!");

        lblChangeEmailSuccess2.setFont(new java.awt.Font("Tahoma", 1, 11));
        lblChangeEmailSuccess2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblChangeEmailSuccess2.setText("Consulte a sua caixa de correio.");

        btnChangeEmailOk.setText("OK");
        btnChangeEmailOk.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnChangeEmailOkMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout ifChangeEmailSuccessLayout = new javax.swing.GroupLayout(ifChangeEmailSuccess.getContentPane());
        ifChangeEmailSuccess.getContentPane().setLayout(ifChangeEmailSuccessLayout);
        ifChangeEmailSuccessLayout.setHorizontalGroup(
            ifChangeEmailSuccessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblChangeEmailSuccess1, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
            .addGroup(ifChangeEmailSuccessLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblChangeEmailSuccess2, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(ifChangeEmailSuccessLayout.createSequentialGroup()
                .addGap(95, 95, 95)
                .addComponent(btnChangeEmailOk, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(94, Short.MAX_VALUE))
        );
        ifChangeEmailSuccessLayout.setVerticalGroup(
            ifChangeEmailSuccessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ifChangeEmailSuccessLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(lblChangeEmailSuccess1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblChangeEmailSuccess2)
                .addGap(18, 18, 18)
                .addComponent(btnChangeEmailOk)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        panelChangeEmail.add(ifChangeEmailSuccess, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 150, 260, 160));

        panelChangeEmailFields.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Alterar Email", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.BELOW_TOP));

        lblCurrentEmail.setText("Email corrente:");

        lblNewEmail.setText("Novo email:");

        javax.swing.GroupLayout panelChangeEmailFieldsLayout = new javax.swing.GroupLayout(panelChangeEmailFields);
        panelChangeEmailFields.setLayout(panelChangeEmailFieldsLayout);
        panelChangeEmailFieldsLayout.setHorizontalGroup(
            panelChangeEmailFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelChangeEmailFieldsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelChangeEmailFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCurrentEmail)
                    .addComponent(lblNewEmail))
                .addGap(18, 18, 18)
                .addGroup(panelChangeEmailFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tfNewMailText)
                    .addComponent(lblCurrentEmailText, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        panelChangeEmailFieldsLayout.setVerticalGroup(
            panelChangeEmailFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelChangeEmailFieldsLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(panelChangeEmailFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCurrentEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCurrentEmailText, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panelChangeEmailFieldsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNewEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfNewMailText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(87, Short.MAX_VALUE))
        );

        panelChangeEmail.add(panelChangeEmailFields, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 440, 210));

        btnValidateEmail.setText("Alterar");
        btnValidateEmail.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnValidateEmailMouseReleased(evt);
            }
        });
        panelChangeEmail.add(btnValidateEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(251, 490, 80, 30));

        btnCancelNewEmail.setText("Cancelar");
        btnCancelNewEmail.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnCancelNewEmailMouseReleased(evt);
            }
        });
        panelChangeEmail.add(btnCancelNewEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(341, 490, 80, 30));

        menuPanel.add(panelChangeEmail);
        panelChangeEmail.setBounds(250, 25, 440, 520);

        lblLogo1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pt/uac/cafeteria/ui/images/logo.png"))); // NOI18N
        menuPanel.add(lblLogo1);
        lblLogo1.setBounds(700, 500, 100, 100);

        lblFrontBK1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/pt/uac/cafeteria/ui/images/frontBK.png"))); // NOI18N
        menuPanel.add(lblFrontBK1);
        lblFrontBK1.setBounds(0, 0, 939, 683);

        separator.setOrientation(javax.swing.SwingConstants.VERTICAL);
        menuPanel.add(separator);
        separator.setBounds(205, 0, 10, 611);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(menuPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(menuPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConfirmMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConfirmMouseReleased
        int accountNumber = changeStringToInt(tfNumber.getText());
        int password = changeStringToInt(String.valueOf(pfPinCode.getPassword()));
                
        try {
            student = Application.authenticateStudent(accountNumber, password);
                        
            if (btnConfirm.isEnabled() && student != null) {
                mainPanel.setVisible(false);
                menuPanel.setVisible(true);
                panelWelcome.setVisible(true);
                panelBuyTicket.setVisible(false);
                panelCheckBalance.setVisible(false);
                panelChangePinCode.setVisible(false);
                panelChangeEmail.setVisible(false);
                panelChooseDay.setVisible(false);
                ifLogOut.setVisible(false);
                btnBuyTicket.setEnabled(true);
                btnCheckBalance.setEnabled(true);
                btnChangePinCode.setEnabled(true);
                btnChangeEmail.setEnabled(true);
                btnLogOut.setEnabled(true);
            }
            else {
                ifLogInFailed.setVisible(true);
                btnConfirm.setEnabled(false);
                tfNumber.setEnabled(false);
                pfPinCode.setEnabled(false);
                lblLogInFailed.setText("Dados Inválidos! Tente novamente.");
           }
        }
        catch (Exception e) {
           if (e instanceof IllegalStateException) {
                ifLogInFailed.setVisible(true);
                btnConfirm.setEnabled(false);
                tfNumber.setEnabled(false);
                pfPinCode.setEnabled(false);
                lblLogInFailed.setText(e.getMessage());
           }
        }
    }//GEN-LAST:event_btnConfirmMouseReleased

    private void btnLogOutMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLogOutMouseReleased
        if (btnLogOut.isEnabled()) {
            ifLogOut.setVisible(true);
            deactivate(panelButtons);
            if (panelCheckBalance.isVisible()) {
                tableTransactions.setEnabled(false);
            }
            if (panelChangePinCode.isVisible()) {
                pfCurrentPin.setEnabled(false);
                pfNewPin.setEnabled(false);
                pfConfirmPin.setEnabled(false);
            }
            if (panelChangeEmail.isVisible()) {
                tfNewMailText.setEnabled(false);
            }
        }
    }//GEN-LAST:event_btnLogOutMouseReleased

    private void btnBuyTicketMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuyTicketMouseReleased
        if (btnBuyTicket.isEnabled()) {
            activate(panelChooseDay);
            panelBuyTicket.setVisible(true);
            panelChooseDay.setVisible(true);
            panelChooseDay.setEnabled(true);
            panelShowMeal.setVisible(false);
            btnConfirmMeal.setVisible(true);
            btnConfirmMeal.setEnabled(false);
            btnCancel.setVisible(true);
            btnCancel.setEnabled(true);
            btnBuy.setVisible(false);
            panelSummary.setVisible(false);
            ifNoMeal.setVisible(false);
            ifMoreTickets.setVisible(false);
            ifPurchaseSuccess.setVisible(false);
            ifCancelBuyTicket.setVisible(false);
            bgTime.clearSelection();
            bgDish.clearSelection();
            deactivate(panelButtons);
            panelWelcome.setVisible(false);
            panelCheckBalance.setVisible(false);
            panelChangePinCode.setVisible(false);
            panelChangeEmail.setVisible(false);
            
            for (int i = 0; i < 2; i++) {
                cbYearChoice.addItem(today.getYear() + i);
            }
        }
    }//GEN-LAST:event_btnBuyTicketMouseReleased

    private void btnCancelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseReleased
        if (btnCancel.isEnabled()) {
            ifCancelBuyTicket.setVisible(true);
            btnConfirmMeal.setEnabled(false);
            btnBuy.setEnabled(false);
            rbLunch.setEnabled(false);
            rbDinner.setEnabled(false);
            btnBuy.setEnabled(false);
        }
        if (panelShowMeal.isVisible()) {
            rbMeat.setEnabled(false);
            rbFish.setEnabled(false);
            rbVegetarian.setEnabled(false);
        }
    }//GEN-LAST:event_btnCancelMouseReleased

    private void rbMeatMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rbMeatMouseReleased
        btnConfirmMeal.setEnabled(true);
    }//GEN-LAST:event_rbMeatMouseReleased

    private void btnYesTicketsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnYesTicketsMouseReleased
        ifMoreTickets.setVisible(false);
        panelSummary.setVisible(false);
        panelShowMeal.setVisible(false);
        btnBuy.setVisible(false);
        panelChooseDay.setVisible(true);
        btnCancel.setVisible(true);
        btnCancel.setEnabled(true);
        btnConfirmMeal.setVisible(true);
        btnConfirmMeal.setEnabled(false);
        bgTime.clearSelection();
        bgDish.clearSelection();
        activate(panelChooseDay);
        panelChooseDay.setEnabled(true);
    }//GEN-LAST:event_btnYesTicketsMouseReleased

    private void btnNoTicketsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNoTicketsMouseReleased
        if (btnNoTickets.isEnabled()) {
            ifMoreTickets.setVisible(false);
            btnBuy.setVisible(false);
            btnCancel.setVisible(false);
            btnBuyTicket.setEnabled(true);
            btnCheckBalance.setEnabled(true);
            btnChangePinCode.setEnabled(true);
            btnChangeEmail.setEnabled(true);
            btnLogOut.setEnabled(true);
            panelWelcome.setVisible(true);
        }
    }//GEN-LAST:event_btnNoTicketsMouseReleased

    private void btnBuyMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnBuyMouseReleased

        Day day = getSelectedDay();
        Menu menu = MapperRegistry.menu().find(day);
        
        if (btnBuy.isEnabled()) {
            try {
                Meal meal = menu.getMeal(getMealTimeChoice(), getMealTypeChoice());
                double mealPrice = Application.mealPrice(meal, student);
                student.getAccount().buyTicket(meal, mealPrice);
                MapperRegistry.account().update(student.getAccount());

                btnBuy.setEnabled(false);
                btnCancel.setEnabled(false);
                ifPurchaseSuccess.setVisible(true);
            }
            catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }//GEN-LAST:event_btnBuyMouseReleased

    private void btnPurchaseOkMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPurchaseOkMouseReleased
        ifPurchaseSuccess.setVisible(false);
        panelSummary.setVisible(false);
        ifMoreTickets.setVisible(true);
        btnBuy.setVisible(false);
        btnCancel.setVisible(false);
    }//GEN-LAST:event_btnPurchaseOkMouseReleased

    private void btnYesCancelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnYesCancelMouseReleased
        if (btnYesCancel.isEnabled()) {
            panelChooseDay.setVisible(false);
            panelShowMeal.setVisible(false);
            btnConfirmMeal.setVisible(false);
            btnCancel.setVisible(false);
            ifMoreTickets.setVisible(false);
            panelSummary.setVisible(false);
            btnBuy.setVisible(false);
            ifCancelBuyTicket.setVisible(false);
            btnBuyTicket.setEnabled(true);
            btnCheckBalance.setEnabled(true);
            btnChangePinCode.setEnabled(true);
            btnChangeEmail.setEnabled(true);
            btnLogOut.setEnabled(true);
            panelWelcome.setVisible(true);
        }
    }//GEN-LAST:event_btnYesCancelMouseReleased

    private void btnNoCancelMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNoCancelMouseReleased
        ifCancelBuyTicket.setVisible(false);
        rbLunch.setEnabled(true);
        rbDinner.setEnabled(true);

        if (panelShowMeal.isVisible()) {
            deactivate(panelChooseDay);
            rbMeat.setEnabled(true);
            rbFish.setEnabled(true);
            rbVegetarian.setEnabled(true);
        }
        if (rbMeat.isSelected() || rbFish.isSelected() || rbVegetarian.isSelected()) {
            btnConfirmMeal.setEnabled(true);
        }
        if (panelSummary.isVisible()) {
            btnBuy.setEnabled(true);
        }
    }//GEN-LAST:event_btnNoCancelMouseReleased

    private void btnCheckBalanceMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCheckBalanceMouseReleased
        if (btnCheckBalance.isEnabled()) {
            panelWelcome.setVisible(false);
            panelCheckBalance.setVisible(true);
            panelChangePinCode.setVisible(false);
            btnCheckBalance.setEnabled(false);
            btnChangePinCode.setEnabled(true);
            btnChangeEmail.setEnabled(true);
            btnLogOut.setEnabled(true);
            lblNameText.setText(student.getName());
            lblAddressText.setText(student.getAddress().getStreetAddress() +", nº "+student.getAddress().getNumber());
            lblPhoneText.setText(String.valueOf(student.getPhone()));
            lblEmailText.setText(student.getEmail());
            if (student.hasScholarship()) {
                chbScholarship.setSelected(true);
            }
            else {
                chbScholarship.setSelected(false);
            }
            lblCourseText.setText(String.valueOf(student.getCourse()));
            lblAccountText.setText(String.valueOf(student.getAccount().getId()));
            lblBalanceText.setText("€"+String.valueOf(student.getAccount().getBalance()));
            
            DefaultTableModel transTabModel = (DefaultTableModel) tableTransactions.getModel();

            transTabModel.setRowCount(0);
            
            for (Transaction trans : student.getAccount().getTransactions()) {
                transTabModel.addRow(transactionRow(trans));
            }
        }
    }//GEN-LAST:event_btnCheckBalanceMouseReleased

    private void btnChangePinCodeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnChangePinCodeMouseReleased
        if (btnChangePinCode.isEnabled()) {
            panelWelcome.setVisible(false);
            panelChangePinCode.setVisible(true);
            panelCheckBalance.setVisible(false);
            panelChangeEmail.setVisible(false);
            btnCheckBalance.setEnabled(true);
            btnChangePinCode.setEnabled(false);
            btnChangeEmail.setEnabled(true);
            btnLogOut.setEnabled(true);
            ifChangePinCodeSuccess.setVisible(false);
            ifCancelPinCode.setVisible(false);
            btnValidatePinCode.setEnabled(true);
            btnCancelNewPinCode.setEnabled(true);
            pfCurrentPin.setText(null);
            pfNewPin.setText(null);
            pfConfirmPin.setText(null);
            pfCurrentPin.setEnabled(true);
            pfNewPin.setEnabled(true);
            pfConfirmPin.setEnabled(true);
        }
    }//GEN-LAST:event_btnChangePinCodeMouseReleased

    private void btnValidatePinCodeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnValidatePinCodeMouseReleased
        currentPin = String.valueOf(pfCurrentPin.getPassword());
        newPin = String.valueOf(pfNewPin.getPassword());
        confirmPin = String.valueOf(pfConfirmPin.getPassword());
        
        deactivate(panelButtons);
        ifChangePinCodeSuccess.setVisible(true);
        ifChangePinCodeSuccess.requestFocusInWindow();
        btnCancelNewPinCode.setEnabled(false);
        pfCurrentPin.setEnabled(false);
        pfNewPin.setEnabled(false);
        pfConfirmPin.setEnabled(false);
        
        if (!Validator.testDigits(4, ifIsNull(currentPin)) || !Validator.testDigits(4, ifIsNull(newPin)) || !Validator.testDigits(4, ifIsNull(confirmPin))) {
            btnValidatePinCode.setEnabled(false);
            ifChangePinCodeSuccess.setTitle("Aviso");
            lblChangePinCodeSuccess1.setText("Preencha todos os campos correctamente!");
            lblChangePinCodeSuccess2.setText("Exemplo: 1234");
        }
        else {
            if(btnValidatePinCode.isEnabled() && changeStringToInt(currentPin) == student.getAccount().getPinCode() && changeStringToInt(newPin) == changeStringToInt(confirmPin) && !newPin.isEmpty() && !confirmPin.isEmpty()) {
                btnValidatePinCode.setEnabled(false);

                student.getAccount().setPinCode(changeStringToInt(confirmPin));

                String subject = "Alteração do Código de Acesso";
                String body = "Olá, " + student.getName()
                       + "\n\nO seu código de acesso foi alterado com sucesso no Sistema Cafeteria:\n"
                       + "\nNovo Código de Acesso: " + student.getAccount().getPinCode()
                       + "\n\nCom os melhores cumprimentos,\nA Administração.";

                if (MapperRegistry.account().update(student.getAccount())) {
                    try {
                        Application.sendMail(student.getEmail(), subject, body);

                        ifChangePinCodeSuccess.setTitle("Informação");
                        lblChangePinCodeSuccess1.setText("Código de acesso alterado com sucesso!");
                        lblChangePinCodeSuccess2.setText("Consulte a sua caixa de correio.");
                    }
                    catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e.getMessage());
                        lblChangePinCodeSuccess2.setText(null);
                    }
                }
                else {
                    ifChangePinCodeSuccess.setTitle("Aviso");
                    lblChangePinCodeSuccess1.setText("Não foi possível guardar dados!");
                    lblChangePinCodeSuccess2.setText(null);
                }
            }
            else {
                if (changeStringToInt(currentPin) != student.getAccount().getPinCode() && changeStringToInt(newPin) == changeStringToInt(confirmPin)) {
                    ifChangePinCodeSuccess.setTitle("Aviso");
                    lblChangePinCodeSuccess1.setText("Código corrente incorrecto!");
                    lblChangePinCodeSuccess2.setText(null);
                    pfCurrentPin.setText(null);
                }
                if (changeStringToInt(currentPin) == student.getAccount().getPinCode() && changeStringToInt(newPin) != changeStringToInt(confirmPin)) {
                    ifChangePinCodeSuccess.setTitle("Aviso");
                    lblChangePinCodeSuccess1.setText("As palavras-passe não correspondem!");
                    lblChangePinCodeSuccess2.setText(null);
                    pfNewPin.setText(null);
                    pfConfirmPin.setText(null);
                }
                if (changeStringToInt(currentPin) != student.getAccount().getPinCode() && changeStringToInt(newPin) != changeStringToInt(confirmPin)) {
                    ifChangePinCodeSuccess.setTitle("Aviso");
                    lblChangePinCodeSuccess1.setText("Código corrente incorrecto!");
                    lblChangePinCodeSuccess2.setText("As palavras-passe não correspondem!");
                    pfCurrentPin.setText(null);
                    pfNewPin.setText(null);
                    pfConfirmPin.setText(null);
                }
            }
        }
    }//GEN-LAST:event_btnValidatePinCodeMouseReleased

    private void btnCancelNewPinCodeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelNewPinCodeMouseReleased
        if (btnCancelNewPinCode.isEnabled()) {
            deactivate(panelButtons);
            btnCancelNewPinCode.setEnabled(false);
            btnValidatePinCode.setEnabled(false);
            ifCancelPinCode.setVisible(true);
            pfCurrentPin.setEnabled(false);
            pfNewPin.setEnabled(false);
            pfConfirmPin.setEnabled(false);
        }
    }//GEN-LAST:event_btnCancelNewPinCodeMouseReleased

    private void btnChangeEmailMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnChangeEmailMouseReleased
        if (btnChangeEmail.isEnabled()) {
            panelWelcome.setVisible(false);
            panelChangeEmail.setVisible(true);
            panelCheckBalance.setVisible(false);
            panelChangePinCode.setVisible(false);
            btnChangeEmail.setEnabled(false);
            btnCheckBalance.setEnabled(true);
            btnChangePinCode.setEnabled(true);
            btnLogOut.setEnabled(true);
            ifChangeEmailSuccess.setVisible(false);
            ifCancelEmail.setVisible(false);
            btnValidateEmail.setEnabled(true);
            btnCancelNewEmail.setEnabled(true);
            tfNewMailText.setEnabled(true);
            tfNewMailText.setText(null);
            lblCurrentEmailText.setText(student.getEmail());
        }
    }//GEN-LAST:event_btnChangeEmailMouseReleased

    private void btnChangeEmailOkMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnChangeEmailOkMouseReleased
        regex = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        
        if (student.getEmail().equals(tfNewMailText.getText())) {
            panelChangeEmail.setVisible(false);
            panelWelcome.setVisible(true);
            activate(panelButtons);
        }
        else if(!Validator.matchPattern(regex, tfNewMailText.getText())) {
            ifChangeEmailSuccess.setVisible(false);
            btnValidateEmail.setEnabled(true);
            btnCancelNewEmail.setEnabled(true);
            tfNewMailText.setEnabled(true);
            activate(panelButtons);
            btnChangeEmail.setEnabled(false);
        }
        else if (!student.getEmail().equals(tfNewMailText.getText())) {
            ifChangeEmailSuccess.setVisible(false);
            btnValidateEmail.setEnabled(true);
            btnCancelNewEmail.setEnabled(true);
            tfNewMailText.setEnabled(true);
            activate(panelButtons);
            btnChangeEmail.setEnabled(false);
        }
    }//GEN-LAST:event_btnChangeEmailOkMouseReleased

    private void btnValidateEmailMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnValidateEmailMouseReleased
        ifChangeEmailSuccess.setVisible(true);
        btnValidateEmail.setEnabled(false);
        btnCancelNewEmail.setEnabled(false);
        tfNewMailText.setEnabled(false);
        deactivate(panelButtons);
        
        regex = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        
        if (Validator.matchPattern(regex, tfNewMailText.getText()) && MapperRegistry.student().findByEmail(tfNewMailText.getText()) == null) {
            
            student.setEmail(tfNewMailText.getText());
            
            String subject = "Alteração do Endereço de E-mail";
            String body = "Olá, " + student.getName()
                   + "\n\nO seu e-mail foi alterado com sucesso no Sistema Cafeteria:\n"
                   + "\nE-mail actual: " + student.getEmail()
                   + "\n\nCom os melhores cumprimentos,\nA Administração.";
            try {
                if (MapperRegistry.student().update(student)) {
                    try {
                        Application.sendMail(student.getEmail(), subject, body);
                        ifChangeEmailSuccess.setTitle("Informação");
                        lblChangeEmailSuccess1.setText("Email alterado com sucesso!");
                        lblChangeEmailSuccess2.setText("Consulte a sua caixa de correio.");
                    }
                    catch (Exception e){
                        JOptionPane.showMessageDialog(null, e.getMessage());
                        lblChangeEmailSuccess2.setText(null);
                    }
                }
                else {
                    ifChangeEmailSuccess.setTitle("Aviso");
                    lblChangeEmailSuccess1.setText("Não foi possível guardar dados!");
                    lblChangeEmailSuccess2.setText(null);
                }
            }
            catch (ApplicationException e) {
                ApplicationException.log(e);
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        if (!Validator.matchPattern(regex, tfNewMailText.getText())) {
            ifChangeEmailSuccess.setTitle("Aviso");
            lblChangeEmailSuccess1.setText("Introduza um endereço válido!");
            lblChangeEmailSuccess2.setText(null);
        }
        else if (MapperRegistry.student().findByEmail(tfNewMailText.getText()) != null && !student.getEmail().equals(tfNewMailText.getText())) {
            ifChangeEmailSuccess.setTitle("Aviso");
            lblChangeEmailSuccess1.setText("Email já registado!");
            lblChangeEmailSuccess2.setText(null);
        }
    }//GEN-LAST:event_btnValidateEmailMouseReleased

    private void btnCancelNewEmailMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelNewEmailMouseReleased
        if (btnCancelNewEmail.isEnabled()) {
            deactivate(panelButtons);
            tfNewMailText.setEnabled(false);
            ifCancelEmail.setVisible(true);
        }
    }//GEN-LAST:event_btnCancelNewEmailMouseReleased

    private void btnChangePinCodeSuccessOkMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnChangePinCodeSuccessOkMouseReleased
        currentPin = String.valueOf(pfCurrentPin.getPassword());
        newPin = String.valueOf(pfNewPin.getPassword());
        confirmPin = String.valueOf(pfConfirmPin.getPassword());
        
        if (currentPin.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
            activate(panelButtons);
            btnChangePinCode.setEnabled(false);
            ifChangePinCodeSuccess.setVisible(false);
            btnValidatePinCode.setEnabled(true);
            btnCancelNewPinCode.setEnabled(true);
            pfCurrentPin.setEnabled(true);
            pfNewPin.setEnabled(true);
            pfConfirmPin.setEnabled(true);
        }
        else if (!Validator.testDigits(4, ifIsNull(currentPin)) || !Validator.testDigits(4, ifIsNull(newPin)) || !Validator.testDigits(4, ifIsNull(confirmPin))) {
            activate(panelButtons);
            btnChangePinCode.setEnabled(false);
            ifChangePinCodeSuccess.setVisible(false);
            btnValidatePinCode.setEnabled(true);
            btnCancelNewPinCode.setEnabled(true);
            pfCurrentPin.setEnabled(true);
            pfNewPin.setEnabled(true);
            pfConfirmPin.setEnabled(true);
        }
        else {
            ifChangePinCodeSuccess.setVisible(false);
            panelChangePinCode.setVisible(false);         
            btnChangePinCode.setEnabled(true);
            panelWelcome.setVisible(true);
            btnLogOut.setEnabled(true);
            activate(panelButtons);
        }
    }//GEN-LAST:event_btnChangePinCodeSuccessOkMouseReleased

    private void btnYesCancelLogOutMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnYesCancelLogOutMouseReleased
        menuPanel.setVisible(false);
        mainPanel.setVisible(true);
        tfNumber.setText(null);
        pfPinCode.setText(null);
    }//GEN-LAST:event_btnYesCancelLogOutMouseReleased

    private void btnNoCancelLogOutMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNoCancelLogOutMouseReleased
        ifLogOut.setVisible(false);
        if (panelWelcome.isVisible()) {
            activate(panelButtons);
        }
        if (panelCheckBalance.isVisible()) {
            activate(panelButtons);
            btnCheckBalance.setEnabled(false);
            panelCheckBalance.setEnabled(true);
        }
        if (panelChangePinCode.isVisible()) {
            activate(panelButtons);
            btnChangePinCode.setEnabled(false);
            pfCurrentPin.setEnabled(true);
            pfNewPin.setEnabled(true);
            pfConfirmPin.setEnabled(true);
        }
        if (panelChangeEmail.isVisible()) {
            activate(panelButtons);
            btnChangeEmail.setEnabled(false);
            tfNewMailText.setEnabled(false);
        }
    }//GEN-LAST:event_btnNoCancelLogOutMouseReleased

    private void btnNoMealMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNoMealMouseReleased
        ifNoMeal.setVisible(false);
        panelChooseDay.setVisible(true);
        activate(panelChooseDay);
        bgTime.clearSelection();
        panelShowMeal.setVisible(false);
    }//GEN-LAST:event_btnNoMealMouseReleased

    private void btnYesCancelPinCodeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnYesCancelPinCodeMouseReleased
        panelChangePinCode.setVisible(false);
        panelWelcome.setVisible(true);
        activate(panelButtons);
    }//GEN-LAST:event_btnYesCancelPinCodeMouseReleased

    private void btnNoCancelPinCodeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNoCancelPinCodeMouseReleased
        activate(panelButtons);
        btnChangePinCode.setEnabled(false);
        ifCancelPinCode.setVisible(false);
        pfCurrentPin.setEnabled(true);
        pfNewPin.setEnabled(true);
        pfConfirmPin.setEnabled(true);
        btnValidatePinCode.setEnabled(true);
        btnCancelNewPinCode.setEnabled(true);
    }//GEN-LAST:event_btnNoCancelPinCodeMouseReleased

    private void btnYesCancelEmailMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnYesCancelEmailMouseReleased
        panelChangeEmail.setVisible(false);
        panelWelcome.setVisible(true);
        activate(panelButtons);
    }//GEN-LAST:event_btnYesCancelEmailMouseReleased

    private void btnNoCancelEmailMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnNoCancelEmailMouseReleased
        activate(panelButtons);
        btnChangeEmail.setEnabled(false);
        ifCancelEmail.setVisible(false);
        tfNewMailText.setEnabled(true);
        btnValidateEmail.setEnabled(true);
        btnCancelNewEmail.setEnabled(true);
    }//GEN-LAST:event_btnNoCancelEmailMouseReleased

    private void btnConfirmMealMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConfirmMealMouseReleased

        Day day = getSelectedDay();
        Menu menu = MapperRegistry.menu().find(day);

        if (btnConfirmMeal.isEnabled()) {
            panelChooseDay.setVisible(false);
            panelShowMeal.setVisible(false);
            panelSummary.setVisible(true);
            btnConfirmMeal.setVisible(false);
            btnBuy.setVisible(true);
            btnBuy.setEnabled(true);

            Meal.Time mealTime = getMealTimeChoice();
            Meal.Type mealType = getMealTypeChoice();

            Meal meal = menu.getMeal(mealTime, mealType);
            double mealPrice = Application.mealPrice(meal, student);

            lblPurchaseDateText.setText(today.toString());
            lblPriceText.setText(String.format("€%.2f", mealPrice));

            lblTicketMealDateText.setText(day.toString());
            lbTicketlMealTimeText.setText(mealTime.toString());
            
            lblTicketDish.setText(mealType + ":");
            lblTicketDishText.setText(meal.getMainCourse());
            lblTicketSoupText.setText(meal.getSoup());
            lblDessertText.setText(meal.getDessert());
        }
    }//GEN-LAST:event_btnConfirmMealMouseReleased

    private void btnLogInFailedMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnLogInFailedMouseReleased
        ifLogInFailed.setVisible(false);
        tfNumber.setEnabled(true);
        pfPinCode.setEnabled(true);
        btnConfirm.setEnabled(true);
    }//GEN-LAST:event_btnLogInFailedMouseReleased

    private void pfPinCodeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_pfPinCodeKeyReleased
        int accountNumber = changeStringToInt(tfNumber.getText());
        int password = changeStringToInt(String.valueOf(pfPinCode.getPassword()));
        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                student = Application.authenticateStudent(accountNumber, password);

                if (btnConfirm.isEnabled() && student != null ) {
                    mainPanel.setVisible(false);
                    menuPanel.setVisible(true);
                    panelWelcome.setVisible(true);
                    panelBuyTicket.setVisible(false);
                    panelCheckBalance.setVisible(false);
                    panelChangePinCode.setVisible(false);
                    panelChangeEmail.setVisible(false);
                    panelChooseDay.setVisible(false);
                    ifLogOut.setVisible(false);
                    btnBuyTicket.setEnabled(true);
                    btnCheckBalance.setEnabled(true);
                    btnChangePinCode.setEnabled(true);
                    btnChangeEmail.setEnabled(true);
                    btnLogOut.setEnabled(true);
                }
                else {
                    ifLogInFailed.setVisible(true);
                    btnConfirm.setEnabled(false);
                    tfNumber.setEnabled(false);
                    pfPinCode.setEnabled(false);
                    lblLogInFailed.setText("Dados Inválidos! Tente novamente.");
               }
            }
            catch (Exception e) {
               if (e instanceof IllegalStateException) {
                    ifLogInFailed.setVisible(true);
                    btnConfirm.setEnabled(false);
                    tfNumber.setEnabled(false);
                    pfPinCode.setEnabled(false);
                    lblLogInFailed.setText(e.getMessage());
               }
            }
        }
    }//GEN-LAST:event_pfPinCodeKeyReleased

    private void btnLogInFailedKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnLogInFailedKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            ifLogInFailed.setVisible(false);
            tfNumber.setEnabled(true);
            pfPinCode.setEnabled(true);
            btnConfirm.setEnabled(true);
        }
    }//GEN-LAST:event_btnLogInFailedKeyReleased

    private void cbYearChoiceItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbYearChoiceItemStateChanged
       updateDayItems();
    }//GEN-LAST:event_cbYearChoiceItemStateChanged

    private void cbMonthChoiceItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbMonthChoiceItemStateChanged
       updateDayItems();
    }//GEN-LAST:event_cbMonthChoiceItemStateChanged

    private void cbDayChoiceItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbDayChoiceItemStateChanged

        checkMealTime();

        if (rbLunch.isEnabled() && rbLunch.isSelected() || rbDinner.isEnabled() && rbDinner.isSelected()) {
            panelShowMeal.setVisible(true);
        }
        else {
            bgTime.clearSelection();
            panelShowMeal.setVisible(false);
        }

        updateMealChoices();
    }//GEN-LAST:event_cbDayChoiceItemStateChanged

    private void rbLunchMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rbLunchMouseReleased
        updateMealChoices();
    }//GEN-LAST:event_rbLunchMouseReleased

    private void rbDinnerMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rbDinnerMouseReleased
        updateMealChoices();
    }//GEN-LAST:event_rbDinnerMouseReleased

    private void rbFishMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rbFishMouseReleased
        btnConfirmMeal.setEnabled(true);
    }//GEN-LAST:event_rbFishMouseReleased

    private void rbVegetarianMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rbVegetarianMouseReleased
        btnConfirmMeal.setEnabled(true);
    }//GEN-LAST:event_rbVegetarianMouseReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Frontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Frontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Frontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Frontend.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        try {
            Application.init();
            MapperRegistry.account().setAutosave(true);
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new Frontend().setVisible(true);
                }
            }); 
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());

            ApplicationException.log(e);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgDish;
    private javax.swing.ButtonGroup bgTime;
    private javax.swing.JButton btnBuy;
    private javax.swing.JButton btnBuyTicket;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCancelNewEmail;
    private javax.swing.JButton btnCancelNewPinCode;
    private javax.swing.JButton btnChangeEmail;
    private javax.swing.JButton btnChangeEmailOk;
    private javax.swing.JButton btnChangePinCode;
    private javax.swing.JButton btnChangePinCodeSuccessOk;
    private javax.swing.JButton btnCheckBalance;
    private javax.swing.JButton btnConfirm;
    private javax.swing.JButton btnConfirmMeal;
    private javax.swing.JButton btnLogInFailed;
    private javax.swing.JButton btnLogOut;
    private javax.swing.JButton btnNoCancel;
    private javax.swing.JButton btnNoCancelEmail;
    private javax.swing.JButton btnNoCancelLogOut;
    private javax.swing.JButton btnNoCancelPinCode;
    private javax.swing.JButton btnNoMeal;
    private javax.swing.JButton btnNoTickets;
    private javax.swing.JButton btnPurchaseOk;
    private javax.swing.JButton btnValidateEmail;
    private javax.swing.JButton btnValidatePinCode;
    private javax.swing.JButton btnYesCancel;
    private javax.swing.JButton btnYesCancelEmail;
    private javax.swing.JButton btnYesCancelLogOut;
    private javax.swing.JButton btnYesCancelPinCode;
    private javax.swing.JButton btnYesTickets;
    private javax.swing.JComboBox cbDayChoice;
    private javax.swing.JComboBox cbMonthChoice;
    private javax.swing.JComboBox cbYearChoice;
    private javax.swing.JCheckBox chbScholarship;
    private javax.swing.JInternalFrame ifCancelBuyTicket;
    private javax.swing.JInternalFrame ifCancelEmail;
    private javax.swing.JInternalFrame ifCancelPinCode;
    private javax.swing.JInternalFrame ifChangeEmailSuccess;
    private javax.swing.JInternalFrame ifChangePinCodeSuccess;
    private javax.swing.JInternalFrame ifLogInFailed;
    private javax.swing.JInternalFrame ifLogOut;
    private javax.swing.JInternalFrame ifMoreTickets;
    private javax.swing.JInternalFrame ifNoMeal;
    private javax.swing.JInternalFrame ifPurchaseSuccess;
    private javax.swing.JLabel lbTicketlMealTimeText;
    private javax.swing.JLabel lblAccount;
    private javax.swing.JLabel lblAccountText;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblAddressText;
    private javax.swing.JLabel lblBalance;
    private javax.swing.JLabel lblBalanceText;
    private javax.swing.JLabel lblCancelBuyTickets;
    private javax.swing.JLabel lblCancelEmail;
    private javax.swing.JLabel lblCancelPinCode;
    private javax.swing.JLabel lblChangeEmailSuccess1;
    private javax.swing.JLabel lblChangeEmailSuccess2;
    private javax.swing.JLabel lblChangePinCodeSuccess1;
    private javax.swing.JLabel lblChangePinCodeSuccess2;
    private javax.swing.JLabel lblChooseDish;
    private javax.swing.JLabel lblConfiirmPin;
    private javax.swing.JLabel lblCourse;
    private javax.swing.JLabel lblCourseText;
    private javax.swing.JLabel lblCurrentEmail;
    private javax.swing.JLabel lblCurrentEmailText;
    private javax.swing.JLabel lblCurrentPin;
    private javax.swing.JLabel lblDessert;
    private javax.swing.JLabel lblDessertText;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblEmailText;
    private javax.swing.JLabel lblFish;
    private javax.swing.JLabel lblFishText;
    private javax.swing.JLabel lblFrontBK;
    private javax.swing.JLabel lblFrontBK1;
    private javax.swing.JLabel lblLogInFailed;
    private javax.swing.JLabel lblLogOut1;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblLogo1;
    private javax.swing.JLabel lblMeat;
    private javax.swing.JLabel lblMeatText;
    private javax.swing.JLabel lblMoreTickets2;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblNameText;
    private javax.swing.JLabel lblNewEmail;
    private javax.swing.JLabel lblNewPin;
    private javax.swing.JLabel lblNoMeal;
    private javax.swing.JLabel lblNumber;
    private javax.swing.JLabel lblPhone;
    private javax.swing.JLabel lblPhoneText;
    private javax.swing.JLabel lblPinCode;
    private javax.swing.JLabel lblPrice;
    private javax.swing.JLabel lblPriceText;
    private javax.swing.JLabel lblPurchaseDate;
    private javax.swing.JLabel lblPurchaseDateText;
    private javax.swing.JLabel lblPurchaseSuccess;
    private javax.swing.JLabel lblScholarship;
    private javax.swing.JLabel lblSoup;
    private javax.swing.JLabel lblSoupText;
    private javax.swing.JLabel lblTicketDessert;
    private javax.swing.JLabel lblTicketDessertText;
    private javax.swing.JLabel lblTicketDish;
    private javax.swing.JLabel lblTicketDishText;
    private javax.swing.JLabel lblTicketMealDate;
    private javax.swing.JLabel lblTicketMealDateText;
    private javax.swing.JLabel lblTicketMealTime;
    private javax.swing.JLabel lblTicketSoup;
    private javax.swing.JLabel lblTicketSoupText;
    private javax.swing.JLabel lblVegetarian;
    private javax.swing.JLabel lblVegetarianText;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JLabel lblWelcome1;
    private javax.swing.JLabel lblWelcome2;
    private javax.swing.JLabel lblWelcome3;
    private javax.swing.JLayeredPane lpMeal1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel menuPanel;
    private javax.swing.JPanel panelAccount;
    private javax.swing.JPanel panelButtons;
    private javax.swing.JPanel panelBuyTicket;
    private javax.swing.JPanel panelChangeEmail;
    private javax.swing.JPanel panelChangeEmailFields;
    private javax.swing.JPanel panelChangePinCode;
    private javax.swing.JPanel panelChangePinCodeFields;
    private javax.swing.JPanel panelCheckBalance;
    private javax.swing.JPanel panelChooseDay;
    private javax.swing.JPanel panelLogin;
    private javax.swing.JPanel panelShowMeal;
    private javax.swing.JPanel panelStudent;
    private javax.swing.JPanel panelSummary;
    private javax.swing.JPanel panelWelcome;
    private javax.swing.JPasswordField pfConfirmPin;
    private javax.swing.JPasswordField pfCurrentPin;
    private javax.swing.JPasswordField pfNewPin;
    private javax.swing.JPasswordField pfPinCode;
    private javax.swing.JRadioButton rbDinner;
    private javax.swing.JRadioButton rbFish;
    private javax.swing.JRadioButton rbLunch;
    private javax.swing.JRadioButton rbMeat;
    private javax.swing.JRadioButton rbVegetarian;
    private javax.swing.JSeparator separator;
    private javax.swing.JScrollPane spTransactions;
    private javax.swing.JTable tableTransactions;
    private javax.swing.JTextField tfNewMailText;
    private javax.swing.JTextField tfNumber;
    // End of variables declaration//GEN-END:variables
}
