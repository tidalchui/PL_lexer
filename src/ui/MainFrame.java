package ui;

import lexer.*;

import javax.lang.model.type.ErrorType;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MainFrame extends JFrame {
    Object[] name = {"名字", "属性", "值"};
    Object[][] a;
    JTable table;
    JScrollPane jsp_table;
    JTextArea jta, morpheme;
    JPanel jp;
    JTextArea console;
    public MainFrame() {
        Font tip_font = new Font("default",Font.BOLD,15);
        //初始化窗口
        setTitle("词法分析器");
        setBounds(450, 200, 950, 730);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        jp = new JPanel();
        jp.setLayout(null);
        setContentPane(jp);
        //文本域提示标签
        JLabel jta_tip = new JLabel("↓ ↓ ↓ Enter your pascal code");
        jta_tip.setFont(tip_font);
        jta_tip.setBounds(80, 20, 300, 20);
        jp.add(jta_tip);
        //代码文本域
        jta = new JTextArea(10, 23);
        jta.setLineWrap(true);    //设置文本域中的文本为自动换行
        jta.setForeground(Color.BLACK);    //设置组件的背景色
        jta.setFont(new Font("alias", Font.PLAIN, 16));    //修改字体样式
        JScrollPane jsp_code = new JScrollPane(jta);    //将文本域放入滚动窗口
        Dimension size = jta.getPreferredSize();    //获得文本域的首选大小
        jsp_code.setBounds(50, 50, size.width, size.height);
        jp.add(jsp_code);
        //分析button
        JButton analyze = new JButton("分析");
        analyze.setBounds(175, 300, 60, 30);
        analyze.addActionListener(new MyListener());
        jp.add(analyze);
        //词素标签
        JLabel morpheme_tip = new JLabel("词素");
        morpheme_tip.setFont(tip_font);
        morpheme_tip.setBounds(490,20,60,30);
        jp.add(morpheme_tip);
        //词素内容
        morpheme = new JTextArea(12,22);
        morpheme.setBorder(BorderFactory.createLineBorder(Color.black));
        morpheme.setFont(new Font("alias", Font.PLAIN, 14));
        JScrollPane jsp_morpheme = new JScrollPane(morpheme);
        jsp_morpheme.setBounds(380,50,morpheme.getPreferredSize().width,morpheme.getPreferredSize().height);
        jp.add(jsp_morpheme);
        //符号表提示标签
        JLabel table_tip = new JLabel("符号表");
        table_tip.setBounds(758,20,100,30);
        table_tip.setFont(tip_font);
        jp.add(table_tip);
        //符号表
        a = new Object[0][3];
        table = new JTable(a, name);
        table.setFont(new Font("default", Font.PLAIN, 14));
        jsp_table = new JScrollPane(table);
        jsp_table.setViewportView(table);
        jsp_table.setBounds(660, 50, 240, 400);
        jp.add(jsp_table);
        validate();
        //控制台标签
        JLabel console_tip = new JLabel("控制台");
        console_tip.setBounds(50,400,100,30);
        console_tip.setFont(tip_font);
        jp.add(console_tip);
        //控制台内容
        console = new JTextArea(12,80);
        JScrollPane  jsp_console = new JScrollPane(console);
        jsp_console.setBounds(50,450,console.getPreferredSize().width,console.getPreferredSize().height);
        jp.add(jsp_console);
        setVisible(true);

    }

    class MyListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //清空遗留数据
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

            //词素输出
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
            //打印控制台
            console.setText("");
            console.setText(String.valueOf(ErrorWriter.errorList.size())+"error(s)"+"\n");
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

