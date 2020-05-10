package ui;

import lexer.*;
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;

import javax.lang.model.type.ErrorType;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MainFrame extends JFrame {
    Object[] name = {"����", "����", "ֵ"};
    Object[][] a;
    JTable table;
    JScrollPane jsp_table;
    JTextArea jta, morpheme;
    JPanel jp;
    JTextArea console;
    public MainFrame() {
        Font tip_font = new Font("default",Font.BOLD,15);
        //��ʼ������
        setTitle("�ʷ�������");
        setBounds(450, 200, 950, 730);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        jp = new JPanel();
        jp.setLayout(null);
        setContentPane(jp);
        //�ı�����ʾ��ǩ
        JLabel jta_tip = new JLabel("�� �� �� Enter your pascal code");
        jta_tip.setFont(tip_font);
        jta_tip.setBounds(80, 20, 300, 20);
        jp.add(jta_tip);
        //�����ı���
        jta = new JTextArea(10, 23);
        jta.setLineWrap(true);    //�����ı����е��ı�Ϊ�Զ�����
        jta.setForeground(Color.BLACK);    //��������ı���ɫ
        jta.setFont(new Font("alias", Font.PLAIN, 16));    //�޸�������ʽ
        JScrollPane jsp_code = new JScrollPane(jta);    //���ı�������������
        Dimension size = jta.getPreferredSize();    //����ı������ѡ��С
        jsp_code.setBounds(50, 50, size.width, size.height);
        jp.add(jsp_code);
        //����button
        JButton analyze = new JButton("����");
        analyze.setBounds(175, 300, 60, 30);
        analyze.addActionListener(new MyListener());
        jp.add(analyze);
        //���ر�ǩ
        JLabel morpheme_tip = new JLabel("����");
        morpheme_tip.setFont(tip_font);
        morpheme_tip.setBounds(490,20,60,30);
        jp.add(morpheme_tip);
        //��������
        morpheme = new JTextArea(12,22);
        morpheme.setBorder(BorderFactory.createLineBorder(Color.black));
        morpheme.setFont(new Font("alias", Font.PLAIN, 14));
        JScrollPane jsp_morpheme = new JScrollPane(morpheme);
        jsp_morpheme.setBounds(380,50,morpheme.getPreferredSize().width,morpheme.getPreferredSize().height);
        jp.add(jsp_morpheme);
        //���ű���ʾ��ǩ
        JLabel table_tip = new JLabel("���ű�");
        table_tip.setBounds(758,20,100,30);
        table_tip.setFont(tip_font);
        jp.add(table_tip);
        //���ű�
        a = new Object[0][3];
        table = new JTable(a, name);
        table.setFont(new Font("default", Font.PLAIN, 14));
        jsp_table = new JScrollPane(table);
        jsp_table.setViewportView(table);
        jsp_table.setBounds(660, 50, 240, 400);
        jp.add(jsp_table);
        validate();
        //����̨��ǩ
        JLabel console_tip = new JLabel("����̨");
        console_tip.setBounds(50,400,100,30);
        console_tip.setFont(tip_font);
        jp.add(console_tip);
        //����̨����
        console = new JTextArea(12,80);
        JScrollPane  jsp_console = new JScrollPane(console);
        jsp_console.setBounds(50,450,console.getPreferredSize().width,console.getPreferredSize().height);
        jp.add(jsp_console);
        setVisible(true);

    }

    class MyListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //�����������
            SymbolTable.wordItemList.clear();
            SymbolTable.sortCodeList.clear();
            ErrorWriter.errorList.clear();
            String code = jta.getText();
            ParseWords parseWords = new ParseWords();
            try {
                parseWords.doWorkByStr(code);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            a = new Object[SymbolTable.wordItemList.size()][3];
            for(int i = 0 ; i < SymbolTable.wordItemList.size() ; i++) {
                WordItem wordItem = SymbolTable.wordItemList.get(i);
                SortCode sortCode = SymbolTable.getSortCodeByEncode(wordItem.encode);
                if(wordItem.encode<=131) {
                    a[i][0] = wordItem.key;
                    a[i][1] = sortCode.symbol;
                }
                else {
                    a[i][0] = sortCode.word;
                    a[i][1] = sortCode.symbol;
                    a[i][2] = wordItem.key;
                }
            }
            table = new JTable(a,name);
            table.setFont(new Font("default", Font.PLAIN, 14));
            jsp_table.setViewportView(table);

            //�������
            morpheme.setText("");
            int line = 1;
            String lineOut = "";
            for (WordItem wordItem : SymbolTable.wordItemList){
                if(line != wordItem.line) {
                    lineOut = "";
                    morpheme.append("\n");
                    line = wordItem.line;
                }
                lineOut = lineOut + wordItem;
                morpheme.append(wordItem.toString());
            }
            //��ӡ����̨
            console.setText("");
            console.setText(String.valueOf(ErrorWriter.errorList.size())+" error(s)"+"\n");
            if (!ErrorWriter.errorList.isEmpty()){
                for (int i=0; i<ErrorWriter.errorList.size(); i++){
                    console.append(ErrorWriter.getErrorString(i)+"\n");
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new MainFrame();

    }
}

