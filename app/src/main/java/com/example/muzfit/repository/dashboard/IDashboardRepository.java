package com.example.muzfit.repository.dashboard;

import androidx.lifecycle.LiveData;

import com.example.muzfit.model.DashboardCalendarDay;
import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;

import java.util.List;

public interface IDashboardRepository {

    LiveData<Result<Float>> getConsumedCalories();

    // SELECT SUM(p.Calorie) AS CalorieTotali
    // FROM Pasto_has_Utente pu
    // JOIN Pasto p ON p.idPasto = pu.Pasto_idPasto
    // WHERE pu.Utente_Username = <current_user.username>
    // AND DATE(pu.Data) = <today>;
    LiveData<Result<Float>> getConsumedCarbs();

    // SELECT SUM(p.Proteins) AS CalorieTotali
    // FROM Pasto_has_Utente pu
    // JOIN Pasto p ON p.idPasto = pu.Pasto_idPasto
    // WHERE pu.Utente_Username = <current_user.username>
    // AND DATE(pu.Data) = <today>;
    LiveData<Result<Float>> getConsumedProteins();

    // SELECT SUM(p.Fats) AS CalorieTotali
    // FROM Pasto_has_Utente pu
    // JOIN Pasto p ON p.idPasto = pu.Pasto_idPasto
    // WHERE pu.Utente_Username = <current_user.username>
    // AND DATE(pu.Data) = <today>;
    LiveData<Result<Float>> getConsumedFats();

    // SELECT carboidrati, proteine, grassi
    // FROM utente
    // WHERE utente.username = <current_user.username>;
    LiveData<Result<User>> getMacroGoals(String username);

    // SELECT peso.Data, peso.Peso
    // FROM peso
    // WHERE Utente_Username = <current_user.username>;
    LiveData<Result<List<WeightEntry>>> getWeights(String username);

    // SELECT
    //    DATE(a.`Data`) AS Giorno,
    //    SUM(ae.`Calorie`) AS CalorieBruciate
    // FROM `Allenamento` a
    // JOIN `AllenamentoEsercizio` ae
    //    ON ae.`Allenamento_idAllenamento` = a.`idAllenamento`
    //   AND ae.`Allenamento_Utente_Username` = a.`Utente_Username`
    // WHERE a.`Utente_Username` = <current_user.username>
    //  AND DATE(a.`Data`) >= DATE_SUB(<today>, INTERVAL 6 DAY)
    //  AND DATE(a.`Data`) <= <today>
    // GROUP BY DATE(a.`Data`)
    // ORDER BY Giorno;
    LiveData<Result<int[]>> getDailyCaloriesBurned();

    // SELECT
    //    DATE(a.`Data`) AS Giorno,
    //    SUM(ae.`Calorie`) AS CalorieBruciate
    // FROM `Allenamento` a
    // JOIN `AllenamentoEsercizio` ae
    //    ON ae.`Allenamento_idAllenamento` = a.`idAllenamento`
    //   AND ae.`Allenamento_Utente_Username` = a.`Utente_Username`
    // WHERE a.`Utente_Username` = <current_user.username>
    //  AND DATE(a.`Data`) <= <today>
    // GROUP BY DATE(a.`Data`)
    // ORDER BY Giorno;
    LiveData<Result<List<DashboardCalendarDay>>> getCalendarData(int year, int month);
}
