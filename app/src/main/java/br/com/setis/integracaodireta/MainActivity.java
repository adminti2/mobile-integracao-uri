package br.com.setis.integracaodireta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;

import br.com.setis.integracaodireta.databinding.ActivityMainBinding;
public class MainActivity extends AppCompatActivity {
    private String saidaTransacao = "";
    private String transacaoPendente = "";
    private String TAG = "MainActivity";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.tvUri.setVisibility(View.INVISIBLE);
        binding.btnMenuAdmin.setOnClickListener(v -> chamaMenuAdministrativo());
        binding.btnVenda.setOnClickListener(v -> chamaVenda());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        exibeDados(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
    public void chamaMenuAdministrativo() {
        String dadosAdm = "app://payment/input?transactionId=12345&operation=ADMINISTRATIVA";
        Uri uri = Uri.parse(dadosAdm);
        iniciaTransacao(uri);
    }
    public void chamaVenda() {
        String dadosVenda = "app://payment/input?" +
                "operation=VENDA&" +
                "currencyCode=986&" +
                "amount=2000&" +
                "boardingTax=200&" +
                "serviceTax=200&" +
                "cardType=CARTAO_CREDITO&" +
                "finType=PARCELADO_EMISSOR&" +
                "paymentMode=PAGAMENTO_CARTAO&" +
                "installments=2";
        Uri uri = Uri.parse(dadosVenda);
        iniciaTransacao(uri);
    }

    private void iniciaTransacao(Uri uri) {
        Intent transac = new Intent("br.com.setis.payment.TRANSACTION", uri);
        String dadosAutomacao = "app://payment/posData?" +
                "posDeveloper=PAYGO&" +
                "posName=Automação&" +
                "allowDueAmount=true&" +
                "allowDiscount=true&" +
                "allowCashback=true&" +
                "allowShortReceipt=false&" +
                "allowDifferentReceipts=true&" +
                "posVersion=1.0.0";

        String personalizacao = "app://payment/posCustomization?" +
                "fontColor=%23000000&" +
                "keyboardFontColor=%23000000&" +
                "editboxBackgroundColor=%23FFFFFF&" +
                "keyboardBackgroundColor=%23F4F4F4&" +
                "screenBackgroundColor=%23F4F4F4&" +
                "toolbarBackgroundColor=%23242424&" +
                "toolbarTextColor=%23FFFFFF&" +
                "menuSeparatorColor=%23F4F4F4&" +
                "releasedKeyColor=%23dedede&" +
                "pressedKeyColor=%23e1e1e1&" +
                "editboxTextColor=%23000000";

        transac.putExtra("DadosAutomacao", dadosAutomacao);
        transac.putExtra("Personalizacao", personalizacao);
        transac.putExtra("package", getPackageName());
        transac.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(transac);
    }

    private void exibeDados(Intent intent) {
        if (intent.getData() != null) { //retornou do pg integrado

            try {
                saidaTransacao = java.net.URLDecoder.decode(
                        intent.getData().toString(), StandardCharsets.UTF_8.name());
                Log.d(TAG, "SaidaTransacao: " + saidaTransacao);
                binding.tvUri.setText(saidaTransacao);

                transacaoPendente = intent.getStringExtra("TransacaoPendenteDados");
                if (transacaoPendente != null) {
                    Log.d(TAG, "TransacaoPendenteDados: " + transacaoPendente);
                    Intent transacao = startConfirmacao(transacaoPendente);
                    transacao.putExtra("Confirmacao", "app://resolve/confirmation?transactionStatus=CONFIRMADO_AUTOMATICO");
                    this.sendBroadcast(transacao);
                } else {
                    String confirmacao = verificaConfirmacao(saidaTransacao);
                    if (confirmacao != null) {
                        Intent transacao = startConfirmacao(confirmacao);
                        this.sendBroadcast(transacao);
                    }
                }
                binding.tvUri.setVisibility(View.VISIBLE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else {
            binding.tvUri.setVisibility(View.VISIBLE);
        }
    }

    private String verificaConfirmacao(String saidaTransacao) {
        String[] listaDados = saidaTransacao.split("[?]")[1].split("&");
        boolean requiresConfirmation = false;
        String confirmTransactionIdentifier = null;

        for (String dado : listaDados) {
            if (dado.contains("requiresConfirmation") && dado.split("=")[1].equals("true")) {
                requiresConfirmation = true;
            } else if (dado.contains("confirmationTransactionId")) {
                confirmTransactionIdentifier = dado.split("=")[1];
            }
        }
        if (requiresConfirmation && confirmTransactionIdentifier != null) {
            return "app://confirmation/confirmation?" +
                    "confirmationTransactionId=" + confirmTransactionIdentifier + "&" +
                    "transactionStatus=CONFIRMADO_AUTOMATICO";
        }
        return null;
    }

    private Intent startConfirmacao(String confirmacao) {
        Uri uri = Uri.parse(confirmacao);
        Intent transacao = new Intent();
        transacao.setAction("br.com.setis.confirmation.TRANSACTION");
        transacao.putExtra("uri", uri.toString());
        transacao.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        return transacao;
    }

    /**
     * Faz a conversão da imagem de String para Bitmap
     *
     * @param imagemString
     * @return bitmap
     */
    public Bitmap stringToBitmap(String imagemString) {
        try {
            byte[] encodeByte = Base64.decode(imagemString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}