package com.example.muzfit.service;

import com.example.muzfit.service.dto.AllenamentoDto;
import com.example.muzfit.service.dto.AllenamentoEsercizioDto;
import com.example.muzfit.service.dto.DescrizioneEsercizioDto;
import com.example.muzfit.service.dto.PastoDto;
import com.example.muzfit.service.dto.PastoUtenteDto;
import com.example.muzfit.service.dto.PesoDto;
import com.example.muzfit.service.dto.SerieDto;
import com.example.muzfit.service.dto.UtenteDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MuzFitApiService {

    @GET("utente")
    Call<List<UtenteDto>> getUtenti();

    @GET("utente/single")
    Call<UtenteDto> getUtenteSingle();

    @GET("pasto")
    Call<List<PastoDto>> getPasti();

    @GET("pasto/single")
    Call<PastoDto> getPastoSingle();

    @GET("pasto-utente")
    Call<List<PastoUtenteDto>> getPastiUtente();

    @GET("pasto-utente/single")
    Call<PastoUtenteDto> getPastoUtenteSingle();

    @GET("allenamento")
    Call<List<AllenamentoDto>> getAllenamenti();

    @GET("allenamento/single")
    Call<AllenamentoDto> getAllenamentoSingle();

    @GET("descrizione-esercizio")
    Call<List<DescrizioneEsercizioDto>> getDescrizioniEsercizio();

    @GET("descrizione-esercizio/single")
    Call<DescrizioneEsercizioDto> getDescrizioneEsercizioSingle();

    @GET("allenamento-esercizio")
    Call<List<AllenamentoEsercizioDto>> getAllenamentiEsercizio();

    @GET("allenamento-esercizio/single")
    Call<AllenamentoEsercizioDto> getAllenamentoEsercizioSingle();

    @GET("serie")
    Call<List<SerieDto>> getSerie();

    @GET("serie/single")
    Call<SerieDto> getSerieSingle();

    @GET("peso")
    Call<List<PesoDto>> getPesi();

    @GET("peso/single")
    Call<PesoDto> getPesoSingle();
}
