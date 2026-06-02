USE `mydb`;

INSERT INTO `Utente`
    (`Username`, `Nome`, `Password`, `Peso`, `Altezza`, `Genere`, `CalorieBruciate`, `CalorieAssunte`, `Carboidrati`, `Proteine`, `Grassi`)
VALUES
    ('mario.rossi', 'Mario Rossi', 'password123', 78.5, 178.0, 1, 450, 2400, 280.0, 150.0, 70.0),
    ('giulia.verdi', 'Giulia Verdi', 'fit2026', 62.0, 168.0, 0, 360, 1950, 210.0, 120.0, 55.0),
    ('luca.bianchi', 'Luca Bianchi', 'runlift', 84.2, 182.0, 1, 520, 2700, 320.0, 170.0, 80.0);

INSERT INTO `Pasto`
    (`idPasto`, `Alimento`, `Calorie`, `Carboidrati`, `Proteine`)
VALUES
    (1, 'Yogurt greco e avena', 380.0, 52.0, 28.0),
    (2, 'Riso pollo e verdure', 690.0, 82.0, 47.0),
    (3, 'Salmone con patate', 610.0, 48.0, 42.0),
    (4, 'Pasta integrale al tonno', 720.0, 94.0, 39.0),
    (5, 'Frullato proteico banana', 330.0, 44.0, 31.0),
    (6, 'Insalata ceci e feta', 470.0, 46.0, 24.0);

INSERT INTO `DescrizioneEsercizio`
    (`IdEsercizio`, `Descrizione`, `NomeEsercizio`)
VALUES
    (1, 'Spingi il bilanciere dal petto verso l alto mantenendo le scapole addotte.', 'Panca piana'),
    (2, 'Scendi piegando ginocchia e anche mantenendo il busto stabile.', 'Squat'),
    (3, 'Solleva il bilanciere da terra estendendo anche e ginocchia insieme.', 'Stacco da terra'),
    (4, 'Tira il corpo verso la sbarra fino a superarla con il mento.', 'Trazioni'),
    (5, 'Spingi i manubri sopra la testa controllando la fase di discesa.', 'Shoulder press'),
    (6, 'Mantieni il corpo in linea sugli avambracci contraendo addome e glutei.', 'Plank');

INSERT INTO `Allenamento`
    (`idAllenamento`, `Data`, `Descrizione`, `Utente_Username`)
VALUES
    (1, '2026-05-27 18:30:00', 'Forza parte alta', 'mario.rossi'),
    (2, '2026-05-29 19:00:00', 'Gambe pesanti', 'mario.rossi'),
    (3, '2026-05-28 07:15:00', 'Circuito full body', 'giulia.verdi'),
    (4, '2026-05-30 17:45:00', 'Spinta e core', 'giulia.verdi'),
    (5, '2026-05-31 10:00:00', 'Richiamo forza', 'luca.bianchi');

INSERT INTO `AllenamentoEsercizio`
    (`Calorie`, `Allenamento_idAllenamento`, `Allenamento_Utente_Username`, `DescrizioneEsercizio_IdEsercizio`)
VALUES
    (145, 1, 'mario.rossi', 1),
    (110, 1, 'mario.rossi', 4),
    (210, 2, 'mario.rossi', 2),
    (180, 2, 'mario.rossi', 3),
    (95, 3, 'giulia.verdi', 4),
    (120, 3, 'giulia.verdi', 6),
    (130, 4, 'giulia.verdi', 5),
    (85, 4, 'giulia.verdi', 6),
    (200, 5, 'luca.bianchi', 3),
    (160, 5, 'luca.bianchi', 1);

INSERT INTO `Serie`
    (`idSerie`, `Ripetizioni`, `Peso`, `AllenamentoEsercizio_Allenamento_idAllenamento`, `AllenamentoEsercizio_Allenamento_Utente_Username`, `AllenamentoEsercizio_DescrizioneEsercizio_IdEsercizio`)
VALUES
    (1, 8, 70, 1, 'mario.rossi', 1),
    (2, 8, 72, 1, 'mario.rossi', 1),
    (3, 6, 75, 1, 'mario.rossi', 1),
    (1, 8, 0, 1, 'mario.rossi', 4),
    (2, 7, 0, 1, 'mario.rossi', 4),
    (1, 6, 105, 2, 'mario.rossi', 2),
    (2, 6, 110, 2, 'mario.rossi', 2),
    (1, 5, 130, 2, 'mario.rossi', 3),
    (2, 5, 135, 2, 'mario.rossi', 3),
    (1, 10, 0, 3, 'giulia.verdi', 4),
    (2, 8, 0, 3, 'giulia.verdi', 4),
    (1, 45, 0, 3, 'giulia.verdi', 6),
    (2, 50, 0, 3, 'giulia.verdi', 6),
    (1, 10, 16, 4, 'giulia.verdi', 5),
    (2, 10, 18, 4, 'giulia.verdi', 5),
    (1, 60, 0, 4, 'giulia.verdi', 6),
    (1, 5, 150, 5, 'luca.bianchi', 3),
    (2, 4, 155, 5, 'luca.bianchi', 3),
    (1, 6, 92, 5, 'luca.bianchi', 1),
    (2, 5, 95, 5, 'luca.bianchi', 1);

INSERT INTO `Pasto_has_Utente`
    (`Pasto_idPasto`, `Utente_Username`, `Data`)
VALUES
    (1, 'mario.rossi', '2026-05-27 08:00:00'),
    (2, 'mario.rossi', '2026-05-27 13:00:00'),
    (3, 'mario.rossi', '2026-05-27 20:30:00'),
    (5, 'mario.rossi', '2026-05-29 17:45:00'),
    (1, 'giulia.verdi', '2026-05-28 07:45:00'),
    (6, 'giulia.verdi', '2026-05-28 13:15:00'),
    (3, 'giulia.verdi', '2026-05-28 20:00:00'),
    (4, 'luca.bianchi', '2026-05-31 12:30:00'),
    (5, 'luca.bianchi', '2026-05-31 16:30:00'),
    (2, 'luca.bianchi', '2026-05-31 20:15:00');

INSERT INTO `Peso`
    (`Data`, `Peso`, `Utente_Username`)
VALUES
    ('2026-05-01 07:30:00', 79.4, 'mario.rossi'),
    ('2026-05-15 07:30:00', 78.9, 'mario.rossi'),
    ('2026-06-01 07:30:00', 78.5, 'mario.rossi'),
    ('2026-05-01 07:15:00', 62.8, 'giulia.verdi'),
    ('2026-05-15 07:15:00', 62.3, 'giulia.verdi'),
    ('2026-06-01 07:15:00', 62.0, 'giulia.verdi'),
    ('2026-05-01 08:00:00', 85.0, 'luca.bianchi'),
    ('2026-05-15 08:00:00', 84.6, 'luca.bianchi'),
    ('2026-06-01 08:00:00', 84.2, 'luca.bianchi');
