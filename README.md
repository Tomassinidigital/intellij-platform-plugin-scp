# SCP File Synchronizer Plugin

Un plugin per IntelliJ IDEA/PyCharm/PhpStorm che permette di sincronizzare file dal filesystem locale (WSL2) a un server remoto tramite SCP.

## Funzionalità

- **Menu contestuale**: Aggiunge la voce "Sincronizza in remoto" al menu del tasto destro
- **Configurazione esterna**: File di configurazione per parametri del server
- **Copia sicura**: I file originali non vengono mai modificati, viene sempre creata una copia temporanea
- **Log dettagliato**: Monitoraggio in tempo reale delle operazioni
- **Test di connessione**: Verifica della connettività e del trasferimento file

## Configurazione

Vai su **File > Settings > Tools > SCP File Synchronizer** per configurare:

- **Server IP**: Indirizzo IP del server di destinazione
- **Username**: Nome utente per l'accesso SSH/SCP
- **Password**: Password per l'autenticazione
- **Porta**: Porta SSH (default: 22)
- **Cartella di destinazione**: Percorso base sul server remoto
- **Abilita sincronizzazione**: Attiva/disattiva il plugin

## Utilizzo

1. Fai clic destro su un file nel Project Explorer o nell'editor
2. Seleziona "Sincronizza in remoto" dal menu contestuale
3. Il file verrà copiato nella cartella temporanea e trasferito al server
4. Monitora il progresso nella finestra "SCP Sync Log"

## Requisiti

- Sistema operativo con supporto per comandi `scp` e `ssh`
- Per l'autenticazione con password: `sshpass` installato
- WSL2 (se utilizzato su Windows)

## Struttura delle cartelle

Il plugin mantiene la struttura delle cartelle del progetto:
- File del progetto: `/path/to/project/src/main/file.py`
- Destinazione: `[cartella_configurata]/src/main/file.py`

## Log

Tutte le operazioni vengono registrate nella finestra "SCP Sync Log" che include:
- Timestamp delle operazioni
- Creazione di copie temporanee
- Comandi SCP eseguiti
- Risultati delle operazioni
- Messaggi di errore dettagliati

## Sicurezza

- I file originali non vengono mai modificati
- Le copie temporanee vengono eliminate dopo il trasferimento
- Le password sono gestite in modo sicuro (non mostrate nei log)
- Supporto per chiavi SSH (senza password)