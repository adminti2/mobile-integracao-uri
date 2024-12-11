# **Especificação de Integração Direta**

Versão 1.00 (05 mar 2021)

## HISTÓRICO
| **Versão** | **Data** | **Autor** | **Descrição** |
| --- | --- | --- | --- |
| 0.90 | 02 dez 2020 | Marcel Luz | Primeira versão, ainda incompleta, para avaliação do mecanismo. |
| 1.00 | 05 mar 2021 | Vanessa A Gangi | Finalizada primeira versão. |

## ÍNDICE
[**1. Sobre este documento**](#1-sobre-este-documento)
  - [1.1. Escopo](#11-escopo)
  - [1.2. Público](#12-público)

[**2. Arquitetura**](#2-arquitetura)

[**3. Especificação - Integração Direta**](#3-especificação---integração-direta)
  - [3.1. Conceitos básicos](#31-conceitos-básicos)
  - [3.2. Formato da URI](#32-formato-da-uri)
  - [3.3. Parâmetros das Operações](#33-parâmetros-das-operações)
    - [3.3.1. Transação – Requisição](#331-transação--requisição)
    - [3.3.2. Transação – Resposta](#332-transação--resposta)
    - [3.3.3. Confirmação – Requisição](#333-confirmação--requisição)
    - [3.3.4. Resolução de Pendência – Requisição](#334-resolução-de-pendência--requisição)
    - [3.3.5. Dados Automação](#335-dados-automação)
    - [3.3.6. Personalização](#335-dados-automação)
    - [3.3.7. Exemplos](#337-exemplos)

  - [3.4. Fluxo Operacional](#34-fluxo-operacional)
    - [3.4.1. Transação](#341-transação)
    - [3.4.2. Confirmação](#342-confirmação)
    - [3.4.3. Resolução de pendência](#343-resolução-de-pendência)

# 1. Sobre este documento

## 1.1. Escopo
O objetivo deste documento é especificar a integração direta de aplicativos de Automação Comercial, com o aplicativo PayGo Integrado para a plataforma Android.

## 1.2. Público
Este documento destina-se essencialmente a desenvolvedores de aplicativos de Automação Comercial, que por alguma restrição técnica (como linguagem de programação utilizada, ambiente ou até mesmo regra de negócio) não podem utilizar a integração via SDK (Interface Automação).

# 2. Arquitetura
A figura abaixo ilustra a arquitetura do aplicativo PayGo Integrado:

![architecture](https://user-images.githubusercontent.com/11951379/174840699-165cbeaa-69f3-41ff-b292-e1bd9203a208.png)

Na arquitetura ilustrada, a integração entre as aplicações da Automação Comercial e do PayGo Integrado é totalmente abstraída pela biblioteca Interface Automação. Essa biblioteca está escrita em linguagem Java, e é compilada em formato aar (_Android Archive_), que é um padrão da plataforma Android, pois permite a compilação de código e recursos da plataforma (arquivos de _layout_, manifestos, etc.). O objetivo principal dessa biblioteca é abstrair a comunicação entre aplicações, que é feita via _Intents_, deixando a integração com a Automação mais simplificada para desenvolvedores que tenham grande familiaridade com a linguagem Java (ou Kotlin).

Devido ao fato de existirem diversos _frameworks_ para a plataforma Android que permitem o uso de linguagens não nativas para a plataforma Android (Java, Kotlin ou C/C++), a integração via biblioteca aar fica inviabilizada, devido a incompatibilidade desses _frameworks_ com o padrão. De tal maneira que para estes casos, a integração só é possível de ser efetuada de forma direta, ou seja, utilizando o recurso de _Intents_.

# 3. Especificação - Integração Direta

## 3.1. Conceitos básicos
Existem três tipos de operação que podem ser efetuados através do PayGo Integrado:

- Transação (seja ela financeira ou não);
- Confirmação;
- Resolução de pendência.

As operações de Transação podem ser acionadas através do seguinte _Intent Action_:
```java
br.com.setis.payment.TRANSACTION
```
Já as operações de Confirmação/Resolução de pendência devem ser acionadas através do seguinte _Intent Action_:
```java
br.com.setis.confirmation.TRANSACTION
```
A resposta da transação efetuada é retornada pela seguinte _Intent Action_ (a ser tratada pela aplicação através de seu _Manifest_):
```java
br.com.setis.interfaceautomacao.SERVICO
```
Todos os parâmetros transacionais e de confirmação trocados entre Automação Comercial e o aplicativo PayGo Integrado seguem o formato URI (_Universal Resources Identifier_).

## 3.2. Formato da URI
A URI segue o padrão [RFC2396](http://www.faqs.org/rfcs/rfc2396.html). A formatação das URIs trocadas entre a Automação Comercial e o aplicativo PayGo Integrado seguem o seguinte padrão:
```xml
<scheme>://<authority>/<path>?<query>
```
Sendo os componentes identificados a seguir:

| **Componente** | **Formato** |
| --- | --- |
| `<scheme>`    | Fixo: `"app"` |
| `<authority>` | Tipo de operação a ser efetuado, podendo ser:<br/><ul><li>`"payment"`, para operações do tipo Transação;</li><li>`"confirmation"`, para operações do tipo Confirmação;</li><li>`"resolve"`, para operações do tipo Resolução de pendência.</li></ul> |
| `<path>`      | Indica a origem da operação:<br/><ul><li>`"input"`: Indica que a operação está sendo requisitada pela Automação Comercial;</li><li>`"output"`: Indica que é uma resposta de operação requisitada pela automação.</li></ul> |
| `<query>`     | Contém os parâmetros da requisição/resposta, no formato `"<chave>=<valor>"`. Os parâmetros são separados pelo caractere `'&'`. |

## 3.3. Parâmetros das Operações
Para os parâmetros de operação, sejam eles de requisição ou de resposta, serão adotados os seguintes padrões de identificação:

- Quanto à presença:
- M → Indica que o parâmetro é <ins>mandatório</ins>;
- MC → Indica que o parâmetro é <ins>mandatório condicional</ins>. As condições para seu uso estarão descritas juntas à especificação do parâmetro.
- O → Indica que o parâmetro é <ins>opcional</ins>.

- Quanto ao formato:
- N → Indica que o parâmetro é <ins>numérico</ins>;
- AN → Indica que o parâmetro é <ins>alfanumérico</ins> (são aceitos apenas caracteres na faixa ASCII, sem caracteres especiais);
- C → Indica que o parâmetro é uma <ins>constante</ins>. Seus possíveis valores estarão documentados junto ao campo;
- B → Indica que o parâmetro é <ins>booleano</ins> (assume valor "true" se verdadeiro e "false" se falso).

### 3.3.1. Transação – Requisição 
A tabela a seguir indica os parâmetros de requisição para uma transação:

| **Parâmetro** | **Presença** | **Formato** | **Descrição** |
| --- | :---: | :---: | --- |
| operation | M | C | Operação a ser realizada. Valores possíveis:<br/><ul><li>VENDA (operação de venda);</li><li>ADMINISTRATIVA (operação administrativa);</li><li>CANCELAMENTO (operação de cancelamento);</li><li>INSTALACAO (operação de instalação);</li><li>REIMPRESSAO (operação de reimpressão do último comprovante);</li><li>RELATORIO\_SINTETICO (obtém relatório sintético);</li><li>RELATORIO\_DETALHADO (obtém relatório detalhado);</li><li>RELATORIO\_RESUMIDO (obtém relatório resumido);</li><li>TESTE\_COMUNICACAO (realiza teste de comunicação);</li><li>EXIBE\_PDC (exibe o número do ponto de captura);</li><li>VERSAO (exibe a versão instalada);</li><li>CONFIGURACAO (operação de configuração);</li><li>MANUTENCAO (operação de manutenção).</li></ul> |
| transactionId | M | AN | Identificador gerado na automação comercial para a transação. |
| amount | MC | N | Valor da operação. Mandatório se a operação for do tipo VENDA ou CANCELAMENTO. |
| currencyCode | MC | N | Código da moeda, de acordo com ISO4217. Mandatório se operação possuir o parâmetro &quot;amount&quot;. |
| boardingTax | O | N | Taxa de embarque. |
| serviceTax | O | N | Taxa de serviço. |
| provider | O | AN | Nome do provedor utilizado na transação. |
| cardType | O | C | Tipo de cartão. Valores possíveis:<br/><ul><li>CARTAO\_DESCONHECIDO;</li><li>CARTAO\_CREDITO;</li><li>CARTAO\_DEBITO;</li><li>CARTAO\_VOUCHER;</li><li>CARTAO\_PRIVATELABEL;</li><li>CARTAO\_FROTA.</li></ul> |
| finType | O | C | Tipo de financiamento. Valores possíveis:<br/><ul><li>FINANCIAMENTO\_NAO\_DEFINIDO;</li><li>A\_VISTA;</li><li>PARCELADO\_EMISSOR;</li><li>PARCELADO\_ESTABELECIMENTO;</li><li>PRE\_DATADO;</li><li>CREDITO\_EMISSOR.</li></ul> |
| paymentMode | O | C | Modalidade de pagamento. Valores possíveis:<br/><ul><li>PAGAMENTO\_CARTAO;</li><li>PAGAMENTO\_DINHEIRO;</li><li>PAGAMENTO\_CHEQUE;</li><li>PAGAMENTO\_CARTEIRA\_VIRTUAL.</li></ul> |
| installments | O | N | Número de parcelas. |
| predatedDate | O | AN | Data do pré-datado. |
| fiscalDocument | O | AN | Número do documento fiscal. Tamanho máximo: 12 caracteres |
| taxId | O | AN | CPNJ/CPF do estabelecimento. |
| billNumber | O | AN | Número da fatura. |
| phoneNumber | O | AN | Número de telefone, com DDD. |
| posId | O | AN | Identificador do ponto de captura. |
| originalAuthorizationCode | O | AN | Código de autorização original. |
| originalTransactionNsu | O | AN | NSU da transação original. |
| originalTransactionDateTime | O | AN | Data/hora da transação original. |
| additionalPosData1 | O | AN | Dados adicionais relevantes para a automação (#1). |
| additionalPosData2 | O | AN | Dados adicionais relevantes para a automação (#2). |
| additionalPosData3 | O | AN | Dados adicionais relevantes para a automação (#3). |
| additionalPosData4 | O | AN | Dados adicionais relevantes para a automação (#4). |

### 3.3.2. Transação – Resposta
A tabela a seguir indica os parâmetros de resposta para uma transação:

| **Parâmetro** | **Presença** | **Formato** | **Descrição** |
| --- | :---: | :---: | --- |
| operation | M | C | Operação realizada. Valores possíveis:<br/><ul><li>VENDA (operação de venda);</li><li>ADMINISTRATIVA (operação administrativa);</li><li>CANCELAMENTO (operação de cancelamento);</li><li>INSTALACAO (operação de instalação);</li><li>REIMPRESSAO (operação de reimpressão do último comprovante);</li><li>RELATORIO\_SINTETICO (obtém relatório sintético);</li><li>RELATORIO\_DETALHADO (obtém relatório detalhado);</li><li>RELATORIO\_RESUMIDO (obtém relatório resumido);</li><li>TESTE\_COMUNICACAO (realiza teste de comunicação);</li><li>EXIBE\_PDC (exibe o número do ponto de captura);</li><li>VERSAO (exibe a versão instalada);</li><li>CONFIGURACAO (operação de configuração);</li><li>MANUTENÇÃO (operação de manutenção).</li></ul> |
| transactionResult | M | N | Resultado da transação efetuada. |
| amount | MC | N | Valor autorizado, para o caso de uma operação de VENDA. |
| currencyCode | MC | N | Código da moeda, de acordo com ISO4217. Mandatório se operação possuir o parâmetro &quot;amount&quot;. |
| requiresConfirmation | M | B | Indica se a transaçao requer ou não confirmação. |
| confirmationTransactionId | MC | AN | Identificador de confirmação para a transação, caso a confirmação seja requerida. |
| cashbackAmount | O | N | Valor do troco. |
| discountAmount | O | N | Valor do desconto. |
| balanceVoucher | O | N | Saldo do cartão _voucher_. |
| dueAmount | O | N | Valor devido. |
| fiscalDocument | O | AN | Número do documento fiscal. |
| transactionNsu | O | AN | NSU do host. |
| terminalNsu | O | AN | NSU local. |
| authorizationCode | O | AN | Código de autorização. |
| transactionId | O | AN | Identificador da transação para a automação. |
| merchantId | O | AN | Identificador do estabelecimento. |
| posId | O | AN | Identificador do ponto de captura. |
| merchantName | O | AN | Nome do estabelecimento em que o ponto de captura está cadastrado. |
| transactionDateTime | O | AN | Data/hora da transação original. |
| installments | O | N | Número de parcelas. |
| predatedDate | O | AN | Data do pré-datado. |
| finType | O | C | Tipo de financiamento. Valores possíveis:<br/><ul><li>FINANCIAMENTO\_NAO\_DEFINIDO;</li><li>A\_VISTA;</li><li>PARCELADO\_EMISSOR;</li><li>PARCELADO\_ESTABELECIMENTO;</li><li>PRE\_DATADO;</li><li>CREDITO\_EMISSOR.</li></ul> | 
| providerName | O | AN | Nome do provedor. |
| cardType | O | C | Tipo de cartão. Valores possíveis:<br/><ul><li>CARTAO\_DESCONHECIDO;</li><li>CARTAO\_CREDITO;</li><li>CARTAO\_DEBITO;</li><li>CARTAO\_VOUCHER;</li><li>CARTAO\_PRIVATELABEL;</li><li>CARTAO\_FROTA.</li></ul> | 
| cardEntryMode | O | AN | Modo de entrada do cartão. |
| maskedPan | O | AN | Número do cartão, truncado ou mascarado. |
| defaultMaskedPan | O | AN | Número do cartão mascarado no formato BIN + \*\*\* + 4 últimos dígitos. Ex: 543211\*\*\*\*\*\*9876. |
| cardholderVerificationMode | O | AN | Modo de verificação de senha. |
| cardName | O | AN | Nome do cartão ou do emissor do cartão. |
| defaultCardName | O | AN | Descrição do produto bandeira padrão relacionado ao BIN. |
| cardholderName | O | AN | Nome do portador do cartão utilizado. |
| aid | O | AN | Aplicação do cartão utilizada durante a transação. |
| resultMessage | O | AN | Mensagem com descrição do resultado. |
| authorizerResponse | O | AN | Código de resposta da transação, proveniente da rede adquirente. |
| printReceipts | O | C | Vias disponíveis para impressão. Valores possíveis:<br/><ul><li>VIA\_NENHUMA;</li><li>VIA\_CLIENTE;</li><li>VIA\_ESTABELECIMENTO;</li><li>VIA\_CLIENTE\_E\_ESTABELECIMENTO.</li></ul> | 
| fullReceipt | O | AN | Comprovente completo. |
| merchantReceipt | O | AN | Comprovante diferenciado lojista. |
| cardholderReceipt | O | AN | Comprovante diferenciado para o portador. |
| shortReceipt | O | AN | Comprovante reduzido para o portador do cartão. |
| graphicReceiptExists | O | B | Indica existência dos comprovantes no formato gráfico. |
| merchantGraphicReceipt | O | AN | Comprovante gráfico, via do lojista. |
| cardholderGraphicReceipt | O | AN | Comprovante gráfico, via do portador. |
| originalTransactionAmount | O | N | Valor da transação original. |
| originalTransactionDateTime | O | AN | Data/hora da transação original. |
| originalTransactionNsu | O | AN | NSU original do host. |
| originalAuthorizationCode | O | AN | Código de autorização original. |
| originalTerminalNsu | O | AN | NSU local gerado na transação original. |
| pendingTransactionExists | O | B | Indica a existência de transação pendente. |
| authorizationMode | O | C | Modalidade da transação. Valores possíveis:<br/><ul><li>ON;</li><li>OFF.</li></ul> | 
| paymentMode | O | C | Modalidade de pagamento. Valores possíveis:<br/><ul><li>PAGAMENTO\_CARTAO;</li><li>PAGAMENTO\_DINHEIRO;</li><li>PAGAMENTO\_CHEQUE;</li><li>PAGAMENTO\_CARTEIRA\_VIRTUAL.</li></ul> | 
| walletUserId | O | C | Identificação do portador de carteira virtual. Valores possíveis:<br/><ul><li>QRCODE;</li><li>CPF;</li><li>OUTROS.</li></ul> | 
| uniqueId | O | AN | Identificador único da transação armazenado no banco de dados. |

### 3.3.3. Confirmação – Requisição
A tabela a seguir indica os parâmetros de resposta para uma confirmação:

| **Parâmetro** | **Presença** | **Formato** | **Descrição** |
| --- | :---: | :---: | --- |
| transactionStatus | M | C | Status final da transação. Valores possíveis:<br/><ul><li>CONFIRMADO\_AUTOMATICO (transação confirmada sem intervenção do usuário);</li><li>CONFIRMADO\_MANUAL (transação confirmada a pedido do operador);</li><li>DESFEITO\_MANUAL (transação desfeita a pedido do operador).</li></ul> | 
| confirmationTransactionId | M | AN | Identificador de confirmação para a transação (recebido na resposta da transação). |

### 3.3.4. Resolução de Pendência – Requisição
A tabela a seguir indica os parâmetros de resposta para resolução de transação pendente:

| **Parâmetro** | **Presença** | **Formato** | **Descrição** |
| --- | --- | --- | --- |
| providerName | M | AN | Provedor com o qual a transação está pendente. |
| merchantId | M | AN | Identificador do estabelecimento com o qual a transação está pendente. |
| localNsu | M | AN | Obtém o NSU local da transação pendente. |
| transactionNsu | M | AN | Obtém o NSU do servidor TEF da transação pendente. |
| hostNsu | M | AN | Obtém o NSU do provedor da transação pendente. |

### 3.3.5. Dados Automação
Em todas operações do tipo &quot;Transação&quot;, a Automação deve <ins>obrigatoriamente</ins> também informar seus dados como parâmetro.

Esses dados também são enviados como uma URI, porém em um _Bundle_ separado, identificado com a chave "posData". Os seguintes parâmetros devem ser informados:

| **Parâmetro** | **Presença** | **Formato** | **Descrição** |
| --- | --- | --- | --- |
| posName | M | AN | Nome da Automação. |
| posVersion | M | AN | Versão da Automação. |
| posDeveloper | M | AN | Nome da empresa desenvolvedora da Automação Comercial. |
| allowCashback | M | B | Indica se a Automação suporta a funcionalidade de troco. |
| allowDiscount | M | B | Indica se a Automação suporta a funcionalidade de desconto. |
| allowDifferentReceipts | M | B | Indica se a Automação suporta a impressão de vias diferenciadas para o portador e o lojista. |
| allowShortReceipt | M | B | Indica se a Automação suporta a impressão da via reduzida. |
| allowDueAmount | O | B | Indica se a Automação suporta a utilização do saldo total do voucher para abatimento do valor da compra. Se não informado, assume como &quot;falso&quot;. |

### 3.3.6. Personalização
Visando fornecer uma experiência visual menos impactante para o usuário, a Automação Comercial pode customizar elementos de interface do cliente PayGo Integrado, de maneira que este tenha uma identidade visual o mais próximo possível da identidade visual da Automação.

Esses dados também são enviados como uma URI, porém em um _Bundle_ separado, identificado com a chave "posCustomization". Os seguintes parâmetros devem ser informados:

| **Parâmetro** | **Presença** | **Formato** | **Descrição** |
| --- | --- | --- | --- |
| screenBackgroundColor | O | AN | Cor de fundo de tela. |
| keyboardBackgroundColor | O | AN | Cor de fundo do teclado. |
| toolbarBackgroundColor | O | AN | Cor de fundo da barra de ferramentas. |
| fontColor | O | AN | Cor da fonte dos textos. |
| editboxBackgroundColor | O | AN | Cor de fundo da caixa de edição de texto. |
| releasedKeyColor | O | AN | Cor das teclas do teclado virtual da aplicação, quando estiverem liberadas. |
| pressedKeyColor | O | AN | Cor das teclas do teclado virtual da aplicação, quando estiverem pressionadas. |
| keyboardFontColor | O | AN | Cor da fonte do teclado. |
| menuSeparatorColor | O | AN | Cor do separador entre o título de um menu e as opções. |
| toolbarIcon | O | AN | Ícone usado na barra de ferramentas. |
| font | O | AN | Fonte utilizada no texto. |

### 3.3.7. Exemplos

#### Venda
O exemplo a seguir mostra uma URI para uma requisição de venda, no valor de R$1,00:
```java
app://payment/input?currencyCode=986&transactionId=1&amount=100&operation=VENDA
```

#### Dados Automação
O exemplo a seguir mostra uma URI de dados automação:
```java
app://payment/posData?posDeveloper=PAYGO&posName=Automação&allowDueAmount=true&allowDiscount=true&allowCashback=true&allowShortReceipt=false&allowDifferentReceipts=true&posVersion=1.0.0
```

#### Personalização
Para personalizar a aplicação, as cores devem ser enviadas com o valor em hexadecimal, porém para as cores serem reconhecidas na URI, é necessário substituir o # por %23. O exemplo a seguir mostra uma URI de personalização:
```java
app://payment/posCustomization?fontColor=%23000000&keyboardFontColor=%23000000&editboxBackgroundColor=%23FFFFFF&keyboardBackgroundColor=%23F4F4F4&screenBackgroundColor=%23F4F4F4&toolbarBackgroundColor=%232F67F4&menuSeparatorColor=%232F67F4&releasedKeyColor=%23dedede&pressedKeyColor=%23e1e1e1&editboxTextColor=%23000000
```

O tamanho do logo (toolbarIcon) enviado precisa ser menor que _100kb_ podendo ser nos formatos .pgn, .jpg e .bmp.

Para enviar a fonte utilizada no texto (font) e/ou o logo (toolbarIcon) através da URI, é necessário fazer a conversão deles para _bytes_ e, em seguida, codificá-los usando o Base64. O exemplo abaixo, em linguagem Java, ilustra uma maneira de fazer essa conversão:
```java
try {
    File file = (File)f.get("/sdcard/CaviarDreams.ttf");
    if (file == null) {
        return;
    }

    FileInputStream fis = new FileInputStream(file);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];

    int readNum;
    while((readNum = fis.read(buf)) != -1) {
        bos.write(buf, 0, readNum);
    }

    byte[] bytes = bos.toByteArray();
    String value = Base64.encodeToString(bytes, 0);
} catch (Exception ex) {
    ex.printStackTrace();
}
```

#### Confirmação
O exemplo a seguir mostra uma URI para uma requisição de confirmação:
``` java
app://confirmation/confirmation?confirmationTransactionId=0000000000.0000.000000.0000.REDE-SUB&transactionStatus=CONFIRMADO_AUTOMATICO
```

#### Resolução de Pendência
O exemplo a seguir mostra uma URI para uma requisição de resolução de pendência:
``` java
app://resolve/pendingTransaction?merchantId=0000&providerName=REDECARD&hostNsu=000000&localNsu=0000&transactionNsu=0000000000
```

## 3.4. Fluxo Operacional
### 3.4.1. Transação
Devido ao fato de a operação de transação requerer interface com o usuário (para captura dos dados), a _Intent_ deve ser entregue para uma _Activity_, que fará o processamento da interface.

#### Requisição
A requisição deve ser feita através do método _startActivity_. A _Intent_ de parâmetro deve conter os seguintes parâmetros:

- _IntentAction_ e URI dos parâmetros transacionais;
- _Bundle Extra_ dos Dados Automação;
- _Bundle Extra_ da Personalização (se desejado pela Automação Comercial).
- _Bundle Extra_ do nome do pacote da aplicação, sob a chave "package", necessário para que o aplicativo PayGo Integrado consiga efetuar a devolutiva da resposta da transação.
- Para iniciar corretamente a _Activity_, e não causar problemas de _memory leak_, a _Intent_ deve conter as seguintes flags:
- FLAG\_ACTIVITY\_NEW\_TASK;
- FLAG\_ACTIVITY-CLEAR\_TASK.

O exemplo abaixo, em linguagem Java, ilustra uma maneira de iniciar uma transação:
```java
Intent transacao = new Intent("br.com.setis.payment.TRANSACTION", uri);
transacao.putExtra("DadosAutomacao", dadosAutomacao);
transação.putExtra("Personalizacao", personalizacao);
transacao.putExtra("package", getPackageName());
transacao.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
startActivity(transacao);
```

#### Resposta
Para receber a resposta da transação, a aplicação deve implementar um componente para o recebimento da _Intent_ de resposta.

O exemplo abaixo ilustra a configuração necessária no Manifesto para o componente:
```xml
<intent-filter android:label="filter_app_payment">
    <action android:name="br.com.setis.interfaceautomacao.SERVICO"/>
     <category android:name="android.intent.category.DEFAULT"/>
    <category android:name="android.intent.category.BROWSABLE" />

    <data android:scheme="app" android:host="payment" />
    <data android:scheme="app" android:host="resolve" />
</intent-filter>
```

### 3.4.2. Confirmação
Sempre que indicado pela resposta da transação, a Automação Comercial deve efetuar o processo de confirmação da transação. Este processo roda em segundo plano, dentro de um _Broadcast Receiver_ do aplicativo PayGo Integrado, e não possui resposta. Para tal, a requisição deve ser efetuada com o método _sendBroadcast_.

A configuração da _Intent_ de confirmação deve possuir os seguintes parâmetros:
- _IntentAction_;
- Bundle extra com a URI dos parâmetros transacionais;
- Como a aplicação pode estar encerrada durante a execução da confirmação, deve ser incluída a seguinte flag:
- FLAG\_INCLUDE\_STOPPED\_PACKAGES

O exemplo abaixo, em linguagem Java, ilustra uma maneira de efetuar uma confirmação:
```java
Intent transacao = new Intent();
transacao.setAction("br.com.setis.confirmation.TRANSACTION");
transacao.putExtra("uri", uri);
transacao.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
sendBroadcast(transacao);
```

### 3.4.3. Resolução de Pendência
Sempre que indicado na resposta da transação a existência de uma transação pendente, a Automação deve efetuar o processo para confirmar essa transação. Este processo roda em segundo plano, dentro de um _Broadcast Receiver_ do aplicativo PayGo Integrado, e não possui resposta. Para tal, a requisição deve ser efetuada com o método _sendBroadcast_.

A configuração da _Intent_ de resolução de pendência deve possuir os seguintes parâmetros:
- _IntentAction_;
- Bundle extra com a URI dos parâmetros da pendência;
- Bundle extra com a URI dos parâmetros de confirmação;
- Como a aplicação pode estar encerrada durante a execução desta transação, deve ser incluída a seguinte flag:
- Intent.FLAG\_INCLUDE\_STOPPED\_PACKAGES

O exemplo abaixo, em linguagem Java, ilustra uma maneira de efetuar uma resolução de pendência:
```java
Intent transacao = new Intent();
transacao.setAction("br.com.setis.confirmation.TRANSACTION");
transacao.putExtra("uri", uriPendencia);
transacao.putExtra("Confirmacao", "app://resolve/confirmation?transactionStatus=CONFIRMADO_AUTOMATICO");
transacao.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
sendBroadcast(transacao);
```

