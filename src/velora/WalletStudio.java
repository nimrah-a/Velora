package velora;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.*;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/** Original matte-black fintech desktop interface backed by a local educational ledger. */
public final class WalletStudio extends JFrame {
    private static final Color BLACK = new Color(12, 13, 17), SIDE = new Color(17, 19, 25), CARD = new Color(25, 28, 36), CARD2 = new Color(34, 38, 49);
    private static final Color BORDER = new Color(55, 62, 78), WHITE = new Color(247, 250, 255), MUTED = new Color(157, 169, 188);
    private static final Color BLUE = new Color(117, 191, 255), DARKBLUE = new Color(38, 87, 157), PALE = new Color(198, 226, 255);
    private static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    private final CryptoLedger ledger = new CryptoLedger();
    private String selectedWallet, page = "Home", preferredAsset;
    private final JPanel content = new JPanel(new BorderLayout());
    private final JLabel footer = small("Funded local simulation · Not connected to real crypto networks");
    private final Map<String,JButton> navigation = new LinkedHashMap<>();
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault());

    public WalletStudio() {
        super("Velora  |  Digital Finance, Reimagined");
        selectedWallet = ledger.wallets().get(0).address();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1310,850); setMinimumSize(new Dimension(1050,700)); setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(BLACK);
        root.add(sidebar(),BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout()); right.setBackground(BLACK);
        right.add(header(),BorderLayout.NORTH); right.add(content,BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout()); bottom.setBackground(BLACK); bottom.setBorder(new EmptyBorder(9,30,15,30)); bottom.add(footer,BorderLayout.WEST);
        JLabel label = small("DEMO MODE  •  ED25519  •  SHA-256"); label.setForeground(BLUE); bottom.add(label,BorderLayout.EAST);
        right.add(bottom,BorderLayout.SOUTH); root.add(right,BorderLayout.CENTER); setContentPane(root); open("Home");
    }
    private JPanel sidebar(){
        JPanel p = new JPanel(); p.setBackground(SIDE); p.setPreferredSize(new Dimension(222,0)); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(32,18,22,18));
        JLabel mark=txt("◆  velora",26,WHITE,true); mark.setAlignmentX(Component.LEFT_ALIGNMENT); p.add(mark); space(p,4);
        JLabel slogan=small("DIGITAL FINANCE, REIMAGINED"); p.add(slogan); space(p,45);
        for(String item:List.of("Home","Transfer","Wallets","Activity","Blockchain","Security")){
            JButton b=btn("  "+switch(item){case "Home"->"⌂  ";case "Transfer"->"↗  ";case "Wallets"->"▣  ";case "Activity"->"≡  ";case "Blockchain"->"◇  ";default->"◈  ";}+item,false);
            b.setHorizontalAlignment(SwingConstants.LEFT); b.setMaximumSize(new Dimension(188,46)); b.addActionListener(e->open(item)); navigation.put(item,b); p.add(b);space(p,10);
        }
        p.add(Box.createVerticalGlue()); JLabel help=small("JAVA 17  ·  LOCAL LEDGER"); p.add(help); return p;
    }
    private JPanel header(){
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(BLACK); p.setBorder(new EmptyBorder(28,32,24,32));
        JPanel left=new JPanel();left.setOpaque(false);left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));left.add(txt("VELORA / FINANCE STUDIO",11,BLUE,true));space(left,6);left.add(txt("Built to explore. Designed to move.",14,MUTED,false)); p.add(left,BorderLayout.WEST);
        JLabel tag=txt("●  SANDBOX NETWORK",11,PALE,true);tag.setBorder(new EmptyBorder(12,16,12,16)); JPanel chip=card(CARD2);chip.setLayout(new BorderLayout());chip.add(tag);p.add(chip,BorderLayout.EAST);return p;
    }
    private void open(String name){
        page=name;
        navigation.forEach((k,v)->{boolean active=k.equals(name); v.setBackground(active?DARKBLUE:SIDE);v.setForeground(active?WHITE:MUTED);});
        JPanel body=new JPanel();body.setBackground(BLACK);body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));body.setBorder(new EmptyBorder(0,32,20,32));
        switch(name){case "Home"->home(body);case "Transfer"->transfer(body);case "Wallets"->wallets(body);case "Activity"->activity(body);case "Blockchain"->blockchain(body);case "Security"->security(body);}
        JPanel wrap=new JPanel(new BorderLayout());wrap.setBackground(BLACK);wrap.add(body,BorderLayout.NORTH);
        JScrollPane scroll=new JScrollPane(wrap);scroll.setBorder(null);scroll.getViewport().setBackground(BLACK);scroll.getVerticalScrollBar().setUnitIncrement(20);
        content.removeAll();content.add(scroll,BorderLayout.CENTER);content.revalidate();content.repaint();
    }
    private void title(JPanel p,String heading,String subtitle){p.add(txt(heading,30,WHITE,true));space(p,6);p.add(txt(subtitle,13,MUTED,false));space(p,25);}
    private void home(JPanel p){
        title(p,"Your assets. Your control.","Explore functioning wallet transfers using funded sandbox balances.");
        JPanel pick=new JPanel(new BorderLayout(12,0));pick.setOpaque(false);pick.add(txt("CURRENT WALLET",12,PALE,true),BorderLayout.WEST);
        JComboBox<WalletChoice> cb=walletSelect(); cb.setPreferredSize(new Dimension(270,43));cb.addActionListener(e->{WalletChoice w=(WalletChoice)cb.getSelectedItem(); if(w!=null&&!w.address.equals(selectedWallet)){selectedWallet=w.address;open("Home");}});pick.add(cb,BorderLayout.EAST);p.add(pick);space(p,15);
        JPanel hero=card(CARD);hero.setLayout(new BorderLayout(22,0));hero.setBorder(new EmptyBorder(27,29,27,29));
        JPanel numbers=new JPanel();numbers.setOpaque(false);numbers.setLayout(new BoxLayout(numbers,BoxLayout.Y_AXIS));numbers.add(txt("AVAILABLE SIMULATED ASSETS",12,PALE,true));space(numbers,13);
        numbers.add(txt(totalUnits()+" units",35,WHITE,true));space(numbers,9);
        numbers.add(txt("Asset units are not a USD valuation",12,MUTED,false));space(numbers,21);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));actions.setOpaque(false);
        JButton send=btn("↗  Send assets",true);send.addActionListener(e->open("Transfer"));actions.add(send);
        JButton fund=btn("＋  Add demo funds",false);fund.addActionListener(e->fundDialog());actions.add(fund);numbers.add(actions);hero.add(numbers,BorderLayout.CENTER);
        ChartCanvas chart=new ChartCanvas();chart.setPreferredSize(new Dimension(325,175));hero.add(chart,BorderLayout.EAST);p.add(hero);space(p,20);
        p.add(txt("YOUR CRYPTO BALANCES",12,PALE,true));space(p,12);
        JPanel assets=new JPanel(new GridLayout(1,3,14,0));assets.setOpaque(false);
        for(String a:CryptoLedger.ASSETS){JPanel card=card(CARD);card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));card.setBorder(new EmptyBorder(22,20,22,20));
            card.add(txt(symbol(a)+"  "+fullName(a),16,WHITE,true));space(card,20);card.add(txt(amount(ledger.availableBalance(selectedWallet,a))+" "+a,24,WHITE,true));space(card,8);card.add(txt("Available to transfer",12,MUTED,false));space(card,16);
            JButton action=btn("Transfer "+a+"  →",false);action.addActionListener(e->transferDialog(a));card.add(action);assets.add(card);}
        p.add(assets);space(p,21);
        JPanel row=new JPanel(new GridLayout(1,3,14,0));row.setOpaque(false);row.add(stat("CONFIRMED BLOCKS",""+ledger.blocks().size(),"Verified hashes"));row.add(stat("PENDING TRANSFERS",""+ledger.pending().size(),"Ready to confirm"));row.add(stat("ACTIVE WALLETS",""+(ledger.wallets().size()-1),"Excludes treasury"));p.add(row);space(p,22);
        JPanel recent=card(CARD);recent.setLayout(new BoxLayout(recent,BoxLayout.Y_AXIS));recent.setBorder(new EmptyBorder(20,21,20,21));recent.add(txt("LATEST TRANSFERS",15,WHITE,true));space(recent,15);
        List<CryptoLedger.Transfer> transfers=history();int shown=0;
        for(CryptoLedger.Transfer t:transfers){if(t.sender().equals("GENESIS"))continue;if(shown++==4)break;
            recent.add(txt((t.sender().equals(selectedWallet)?"↗ SENT":"↙ RECEIVED")+"   "+amount(t.amount())+" "+t.asset()+"     ·     "+shortId(t.id()),13,WHITE,false));space(recent,13);}
        if(shown==0)recent.add(txt("No transfers yet. Send assets to see activity here.",13,MUTED,false));space(recent,12);
        JButton all=btn("View all activity  →",false);all.addActionListener(e->open("Activity"));recent.add(all);p.add(recent);
    }
    private String totalUnits(){BigDecimal sum=BigDecimal.ZERO;for(String a:CryptoLedger.ASSETS)sum=sum.add(ledger.availableBalance(selectedWallet,a));return amount(sum);}
    private JPanel stat(String label,String number,String desc){JPanel x=card(CARD);x.setBorder(new EmptyBorder(19,19,19,19));x.setLayout(new BoxLayout(x,BoxLayout.Y_AXIS));x.add(txt(label,11,MUTED,true));space(x,10);x.add(txt(number,24,WHITE,true));space(x,8);x.add(txt(desc,12,BLUE,false));return x;}
    private void transfer(JPanel p){title(p,"Move digital assets","Transfer spendable demo funds between wallets, with confirmed balance changes.");
        JPanel explainer=card(CARD2);explainer.setBorder(new EmptyBorder(18,20,18,20));explainer.setLayout(new BorderLayout());explainer.add(txt("01  CHOOSE WALLET     →     02  SELECT ASSET     →     03  CONFIRM TRANSFER",13,PALE,true));p.add(explainer);space(p,22);
        transferForm(p,preferredAsset); preferredAsset = null;
    }
    private void transferForm(JPanel p,String initial){
        JPanel form=card(CARD);form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));form.setBorder(new EmptyBorder(27,27,27,27));
        JComboBox<WalletChoice> sender=walletSelect(),recipient=walletSelect();if(recipient.getItemCount()>1)recipient.setSelectedIndex((sender.getSelectedIndex()+1)%recipient.getItemCount());
        JComboBox<String> asset=combo(CryptoLedger.ASSETS.toArray(new String[0]));if(initial!=null)asset.setSelectedItem(initial);
        JTextField value=field("0.25");JLabel available=txt("",14,BLUE,true);JLabel preview=txt("",13,MUTED,false);
        form.add(txt("FROM",11,PALE,true));space(form,8);form.add(sender);space(form,19);
        form.add(txt("SEND TO",11,PALE,true));space(form,8);form.add(recipient);space(form,19);
        form.add(txt("ASSET",11,PALE,true));space(form,8);form.add(asset);space(form,19);
        form.add(txt("TRANSFER AMOUNT",11,PALE,true));space(form,8);form.add(value);space(form,12);
        JPanel presets=new JPanel(new FlowLayout(FlowLayout.LEFT,9,0));presets.setOpaque(false);
        for(int pct:new int[]{25,50,100}){JButton b=btn(pct==100?"MAX":pct+"%",false);b.addActionListener(e->{WalletChoice w=(WalletChoice)sender.getSelectedItem();if(w==null)return;BigDecimal bal=ledger.availableBalance(w.address,(String)asset.getSelectedItem());
            value.setText(bal.multiply(new BigDecimal(pct)).divide(new BigDecimal("100"),4,RoundingMode.DOWN).stripTrailingZeros().toPlainString());preview.setText("Ready to transfer "+value.getText()+" "+asset.getSelectedItem());});presets.add(b);}
        form.add(presets);space(form,14);form.add(available);space(form,9);form.add(preview);space(form,19);
        Runnable refresh=()->{WalletChoice w=(WalletChoice)sender.getSelectedItem();String a=(String)asset.getSelectedItem();if(w!=null)available.setText("Spendable balance:  "+amount(ledger.availableBalance(w.address,a))+" "+a);};
        sender.addActionListener(e->refresh.run());asset.addActionListener(e->refresh.run());refresh.run();
        JButton confirm=btn("Review & complete transfer   →",true);
        confirm.addActionListener(e->{WalletChoice from=(WalletChoice)sender.getSelectedItem(),to=(WalletChoice)recipient.getSelectedItem();if(from==null||to==null)return;
            try{BigDecimal qty=new BigDecimal(value.getText().trim());String a=(String)asset.getSelectedItem();
                String summary="From: "+from.name+"\nTo: "+to.name+"\nAmount: "+qty.toPlainString()+" "+a+"\n\nA valid Ed25519 signature will be created.\nThe transfer will be confirmed on the local blockchain.";
                if(JOptionPane.showConfirmDialog(this,summary,"Confirm demo transfer",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE)!=JOptionPane.OK_OPTION)return;
                CryptoLedger.Transfer tx=ledger.send(from.address,to.address,a,qty);CryptoLedger.Block block=ledger.minePending();selectedWallet=from.address;
                JOptionPane.showMessageDialog(this,"Transfer confirmed!\n\n"+amount(qty)+" "+a+" moved to "+to.name+".\nBlock #"+block.index()+"\nTransaction: "+tx.id(),"Success",JOptionPane.INFORMATION_MESSAGE);
                footer.setText("Confirmed "+amount(qty)+" "+a+" transfer · Block #"+block.index());open("Home");
            }catch(Exception ex){warning(ex.getMessage());}});form.add(confirm);p.add(form);space(p,14);p.add(note("Wallet balances and transfers are fully functional inside this local demo. No real BTC, ETH, SOL, or USD can enter or leave this app."));
    }
    private void transferDialog(String a){preferredAsset = a; open("Transfer");}
    private void fundDialog(){
        JComboBox<String> asset=combo(CryptoLedger.ASSETS.toArray(new String[0]));
        JPanel x=new JPanel(new GridLayout(0,1,7,8));x.add(new JLabel("Choose an asset to add to your selected wallet:"));x.add(asset);
        x.add(new JLabel("BTC +0.5   •   ETH +2   •   SOL +20"));
        if(JOptionPane.showConfirmDialog(this,x,"Claim spendable demo funds",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
            try{CryptoLedger.Transfer tx=ledger.claimDemoFunds(selectedWallet,(String)asset.getSelectedItem());footer.setText("Demo funds credited · "+amount(tx.amount())+" "+tx.asset());open(page);
                JOptionPane.showMessageDialog(this,"Added "+amount(tx.amount())+" "+tx.asset()+" to your wallet.\nThese units are now spendable in the simulator.","Demo funds received",JOptionPane.INFORMATION_MESSAGE);
            }catch(Exception ex){warning(ex.getMessage());}}
    }
    private void wallets(JPanel p){title(p,"Your wallets","Create, fund, and switch between cryptographic accounts.");
        JPanel add=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));add.setOpaque(false);JTextField name=field("Wallet name");name.setPreferredSize(new Dimension(260,43));JButton create=btn("＋ Create wallet",true);
        create.addActionListener(e->{try{CryptoLedger.Wallet w=ledger.createWallet(name.getText());selectedWallet=w.address();footer.setText("Wallet created: "+w.name());open("Wallets");}catch(Exception ex){warning(ex.getMessage());}});
        JButton fund=btn("＋ Add funds",false);fund.addActionListener(e->fundDialog());add.add(name);add.add(create);add.add(fund);p.add(add);space(p,19);
        for(CryptoLedger.Wallet w:ledger.wallets()){
            if(w.address().equals(ledger.reserveAddress()))continue;
            JPanel row=card(CARD);row.setBorder(new EmptyBorder(18,20,18,20));row.setLayout(new BorderLayout(10,10));
            JPanel info=new JPanel();info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));info.setOpaque(false);boolean active=w.address().equals(selectedWallet);
            info.add(txt((active?"●  ":"○  ")+w.name(),18,active?BLUE:WHITE,true));space(info,7);info.add(txt(w.address(),12,MUTED,false));space(info,8);
            info.add(txt("BTC "+amount(ledger.availableBalance(w.address(),"BTC"))+"    •    ETH "+amount(ledger.availableBalance(w.address(),"ETH"))+"    •    SOL "+amount(ledger.availableBalance(w.address(),"SOL")),13,PALE,false));row.add(info,BorderLayout.CENTER);
            JPanel actions=new JPanel(new GridLayout(2,1,0,7));actions.setOpaque(false);JButton select=btn(active?"Selected ✓":"Select wallet",true);select.addActionListener(e->{selectedWallet=w.address();open("Wallets");});
            JButton copy=btn("Copy address",false);copy.addActionListener(e->{Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(w.address()),null);footer.setText("Address copied");});actions.add(select);actions.add(copy);row.add(actions,BorderLayout.EAST);p.add(row);space(p,12);
        }
        p.add(note("Wallets and private keys exist in memory only. Closing the application resets all demo transactions and balances."));
    }
    private List<CryptoLedger.Transfer> history(){List<CryptoLedger.Transfer> out=new ArrayList<>(ledger.pending());for(CryptoLedger.Block b:ledger.blocks())out.addAll(b.transfers());Collections.reverse(out);return out;}
    private void activity(JPanel p){title(p,"Transaction activity","An inspectable record of fund allocations and transfers.");
        JPanel head=new JPanel(new FlowLayout(FlowLayout.LEFT,9,0));head.setOpaque(false);JButton refresh=btn("↻ Refresh",false);refresh.addActionListener(e->open("Activity"));JButton inspect=btn("Inspect selected transaction",true);head.add(refresh);head.add(inspect);p.add(head);space(p,16);
        List<CryptoLedger.Transfer> rows=history();DefaultTableModel model=new DefaultTableModel(new String[]{"STATUS","ASSET","AMOUNT","FROM","TO","TRANSACTION"},0){public boolean isCellEditable(int r,int c){return false;}};
        Set<String> pending=new HashSet<>();for(CryptoLedger.Transfer t:ledger.pending())pending.add(t.id());
        for(CryptoLedger.Transfer tx:rows)model.addRow(new String[]{pending.contains(tx.id())?"Pending":"Confirmed",tx.asset(),amount(tx.amount()),who(tx.sender()),who(tx.recipient()),shortId(tx.id())});
        JTable table=styledTable(model);JScrollPane scroll=new JScrollPane(table);scroll.setPreferredSize(new Dimension(720,Math.min(470,Math.max(180,45+rows.size()*35))));scroll.getViewport().setBackground(CARD);scroll.setBorder(BorderFactory.createLineBorder(BORDER));p.add(scroll);space(p,15);
        inspect.addActionListener(e->{int i=table.getSelectedRow();if(i<0){warning("Select a transaction first.");return;}CryptoLedger.Transfer tx=rows.get(table.convertRowIndexToModel(i));
            details("Signed transaction", "ID: "+tx.id()+"\n\n"+tx.payload()+"\n\nSignature verified: "+(tx.sender().equals("GENESIS")?"Genesis allocation":CryptoLedger.verifySignature(tx))
                +"\n\nPublic key:\n"+tx.publicKeyBase64()+"\n\nSignature:\n"+tx.signatureBase64());});
        p.add(note("Genesis allocation rows represent sandbox funding. All later transfers use actual Ed25519 digital signatures."));
    }
    private String who(String a){if(a.equals("GENESIS"))return "Initial funding";for(CryptoLedger.Wallet w:ledger.wallets())if(w.address().equals(a))return w.name();return shortId(a);}
    private void blockchain(JPanel p){title(p,"Blockchain explorer","Inspect the local ledger's chain of SHA-256-linked blocks.");
        CryptoLedger.Audit audit=ledger.audit();p.add(note((audit.valid()?"✓ ":"✕ ")+audit.message()));space(p,18);
        List<CryptoLedger.Block> blocks=new ArrayList<>(ledger.blocks());Collections.reverse(blocks);
        for(CryptoLedger.Block b:blocks){JPanel c=card(CARD);c.setLayout(new BorderLayout(10,0));c.setBorder(new EmptyBorder(21,21,21,21));
            JPanel info=new JPanel();info.setOpaque(false);info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));info.add(txt("BLOCK #"+b.index()+(b.index()==0?"  ·  GENESIS":""),18,BLUE,true));space(info,10);
            info.add(txt("Hash   "+b.hash(),12,WHITE,false));space(info,9);info.add(txt("Previous   "+b.previousHash(),12,MUTED,false));space(info,9);
            info.add(txt(b.transfers().size()+" transactions   ·   nonce "+b.nonce()+"   ·   "+TIME.format(Instant.ofEpochMilli(b.timestamp())),12,MUTED,false));c.add(info,BorderLayout.CENTER);
            JButton detail=btn("Inspect",false);detail.addActionListener(e->{StringBuilder s=new StringBuilder("Block #"+b.index()+"\nHash: "+b.hash()+"\nPrevious: "+b.previousHash()+"\nNonce: "+b.nonce());for(CryptoLedger.Transfer tx:b.transfers())s.append("\n\n").append(amount(tx.amount())).append(' ').append(tx.asset()).append("\n").append(who(tx.sender())).append(" → ").append(who(tx.recipient())).append("\n").append(tx.id());details("Block details",s.toString());});c.add(detail,BorderLayout.EAST);p.add(c);space(p,13);}
    }
    private void security(JPanel p){title(p,"Security center","Explore how signing and hash integrity protect this educational blockchain.");
        JPanel stats=new JPanel(new GridLayout(1,3,14,0));stats.setOpaque(false);stats.add(stat("SIGNING ALGORITHM","Ed25519","Real Java cryptography"));stats.add(stat("HASH ALGORITHM","SHA-256","Block integrity"));stats.add(stat("CHAIN LENGTH",ledger.blocks().size()+" blocks","Locally verified"));p.add(stats);space(p,20);
        JPanel c=card(CARD);c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS));c.setBorder(new EmptyBorder(25,25,25,25));c.add(txt("Verify the complete ledger",20,WHITE,true));space(c,12);
        c.add(txt("Checks hashes, previous-block links, valid signatures and balance conservation.",13,MUTED,false));space(c,18);
        JButton verify=btn("Run security audit  →",true);verify.addActionListener(e->{CryptoLedger.Audit a=ledger.audit();JOptionPane.showMessageDialog(this,a.message(),a.valid()?"Audit successful":"Audit failed",a.valid()?JOptionPane.INFORMATION_MESSAGE:JOptionPane.WARNING_MESSAGE);});c.add(verify);space(c,25);
        c.add(txt("Tamper detection experiment",20,WHITE,true));space(c,12);c.add(txt("Modify a copied block's nonce without updating its hash.",13,MUTED,false));space(c,16);
        JButton tamper=btn("Run safe tamper test",false);tamper.addActionListener(e->{List<CryptoLedger.Block> blocks=new ArrayList<>(ledger.blocks());CryptoLedger.Block b=blocks.get(0);blocks.set(0,new CryptoLedger.Block(b.index(),b.timestamp(),b.previousHash(),b.transfers(),b.nonce()+1,b.hash()));
            JOptionPane.showMessageDialog(this,CryptoLedger.auditBlocks(blocks).message()+"\n\nThe live ledger remains untouched.","Tamper detected",JOptionPane.WARNING_MESSAGE);});c.add(tamper);p.add(c);space(p,15);
        p.add(note("Cryptography is real, but BTC/ETH/SOL are demo asset labels. This app is not compatible with Bitcoin, Ethereum or Solana networks."));
    }
    private static JPanel note(String text){JPanel p=card(CARD2);p.setBorder(new EmptyBorder(16,18,16,18));p.setLayout(new BorderLayout());p.add(txt(text,12,PALE,false));return p;}
    private static final class ChartCanvas extends JPanel{
        ChartCanvas(){setOpaque(false);}
        @Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(),h=getHeight();x.setColor(MUTED);x.setFont(new Font("SansSerif",Font.BOLD,11));x.drawString("LEDGER ACTIVITY / ILLUSTRATIVE",8,15);
            double[] points={0.45,0.52,0.46,0.65,0.61,0.79,0.69,0.86,0.81,0.94,0.87,1.0};Path2D path=new Path2D.Double();
            for(int i=0;i<points.length;i++){double px=10+i*(w-20.0)/(points.length-1),py=h-17-points[i]*(h-52);if(i==0)path.moveTo(px,py);else path.lineTo(px,py);}
            x.setStroke(new BasicStroke(3f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));x.setColor(BLUE);x.draw(path);x.setColor(new Color(55,65,84));x.setStroke(new BasicStroke(1f));x.drawLine(10,h-19,w-8,h-19);x.dispose();
        }
    }
    private static JPanel card(Color color){JPanel p=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D x=(Graphics2D)g.create();x.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);x.setColor(getBackground());x.fillRoundRect(0,0,getWidth(),getHeight(),18,18);x.dispose();}};p.setOpaque(false);p.setBackground(color);return p;}
    private static JLabel txt(String t,int size,Color c,boolean bold){JLabel l=new JLabel(t);l.setForeground(c);l.setFont(new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size));l.setAlignmentX(Component.LEFT_ALIGNMENT);return l;}
    private static JLabel small(String t){return txt(t,11,MUTED,false);}
    private static void space(JPanel p,int n){p.add(Box.createVerticalStrut(n));}
    private static JButton btn(String t,boolean primary){JButton b=new JButton(t);b.setFont(new Font("SansSerif",Font.BOLD,13));b.setForeground(primary?BLACK:WHITE);b.setBackground(primary?BLUE:CARD2);b.setOpaque(true);b.setFocusPainted(false);b.setBorderPainted(false);b.setBorder(new EmptyBorder(12,17,12,17));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));return b;}
    private static JTextField field(String tip){JTextField f=new JTextField(22);f.setToolTipText(tip);f.setFont(BODY);f.setForeground(WHITE);f.setCaretColor(BLUE);f.setBackground(CARD2);f.setBorder(new EmptyBorder(13,14,13,14));f.setMaximumSize(new Dimension(Integer.MAX_VALUE,45));return f;}
    private static <T> JComboBox<T> combo(T[] values){JComboBox<T> c=new JComboBox<>(values);c.setFont(BODY);c.setBackground(CARD2);c.setForeground(WHITE);c.setMaximumSize(new Dimension(Integer.MAX_VALUE,45));c.setAlignmentX(Component.LEFT_ALIGNMENT);return c;}
    private record WalletChoice(String name,String address){@Override public String toString(){return name+"  ·  "+shortId(address);}}
    private JComboBox<WalletChoice> walletSelect(){List<WalletChoice> options=new ArrayList<>();for(CryptoLedger.Wallet w:ledger.wallets())if(!w.address().equals(ledger.reserveAddress()))options.add(new WalletChoice(w.name(),w.address()));JComboBox<WalletChoice> c=combo(options.toArray(new WalletChoice[0]));for(int i=0;i<c.getItemCount();i++)if(c.getItemAt(i).address.equals(selectedWallet))c.setSelectedIndex(i);return c;}
    private static String amount(BigDecimal x){return x.stripTrailingZeros().toPlainString();}
    private static String shortId(String x){return x.length()>16?x.substring(0,13)+"…":x;}
    private static String symbol(String a){return switch(a){case "BTC"->"₿";case "ETH"->"Ξ";default->"◎";};}
    private static String fullName(String a){return switch(a){case "BTC"->"Bitcoin";case "ETH"->"Ethereum";default->"Solana";};}
    private static JTable styledTable(DefaultTableModel model){JTable t=new JTable(model);t.setRowHeight(38);t.setFont(BODY);t.setForeground(WHITE);t.setBackground(CARD);t.setSelectionBackground(DARKBLUE);t.setSelectionForeground(WHITE);t.setGridColor(BORDER);t.setShowVerticalLines(false);t.setFillsViewportHeight(true);t.getTableHeader().setBackground(CARD2);t.getTableHeader().setForeground(PALE);t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));t.getTableHeader().setPreferredSize(new Dimension(0,39));return t;}
    private void warning(String s){JOptionPane.showMessageDialog(this,s,"Unable to complete",JOptionPane.WARNING_MESSAGE);}
    private void details(String t,String value){JTextArea area=new JTextArea(value,17,58);area.setEditable(false);area.setLineWrap(true);area.setWrapStyleWord(true);area.setFont(new Font("Monospaced",Font.PLAIN,12));JScrollPane pane=new JScrollPane(area);pane.setPreferredSize(new Dimension(710,360));JOptionPane.showMessageDialog(this,pane,t,JOptionPane.INFORMATION_MESSAGE);}
}
