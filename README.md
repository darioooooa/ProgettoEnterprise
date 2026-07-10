Per poter utilizzare correttamente la nostra app enterprise occorre
modificare gli indirizzi ip nei seguenti file:
    -frontendMobile/app/src/main/java/com/example/enterprisemobile/data/api/RetrofitClient.kt
    -frontendWeb/proxy.conf.json
    -frontendWeb/src/environments/development.ts
    -frontendMobile/app/src/main/java/com/example/enterprisemobile/data/service/ServizioChat.kt
e infine nel file .env che è stato inviato(o su teams o su email)


Per avviare il back-end e i vari servizi utilizzati basta 
lanciare il comando docker compose up -d.

Per avviare il frontendWeb basta lanciare il comando npm start dopo essersi recati nella cartella frontendWeb(con comando cd frontendWeb).
Per avviare il frontendMobile basta aprire Android Studio e cliccare il tasto run.

Altri file relativi a funzionalità(come mappa,servizio pagamento ecc.) sono stati inviati in privato.


