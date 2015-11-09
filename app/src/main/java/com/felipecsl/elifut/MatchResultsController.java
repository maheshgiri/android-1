package com.felipecsl.elifut;

import com.felipecsl.elifut.models.Club;

import rx.Observable;

final class MatchResultsController {
  private final ElifutPreferences preferences;
  private final Club userClub;
  private final Observable<Club> allClubs;

  public MatchResultsController(ElifutPreferences preferences) {
    this.preferences = preferences;
    userClub = preferences.retrieveUserClub();
    allClubs = preferences.retrieveLeagueClubs();
  }

  public void updateByMatchStatistics(MatchStatistics statistics) {
    if (!statistics.isDraw()) {
      Club winner = statistics.winner();

      if (userClub.name().equals(winner.name())) {
        // user is winner
        Club winnerClub = userClub.newWithWin();
        preferences.storeUserClub(winnerClub);
        Observable<Club> observable = allClubs.compose(
            transform(winnerClub, statistics.loser().newWithLoss()));
        preferences.storeLeagueClubs(observable);
      } else {
        // computer is winner
        Club loserClub = userClub.newWithLoss();
        preferences.storeUserClub(loserClub);
        Observable<Club> observable = allClubs.compose(
            transform(statistics.winner().newWithWin(), loserClub));
        preferences.storeLeagueClubs(observable);
      }
    } else {
      // match result is draw
      Club nonUserClub = userClub.name().equals(statistics.home().name())
          ? statistics.away() : statistics.home();
      Club drawClub = userClub.newWithDraw();
      preferences.storeUserClub(drawClub);
      Observable<Club> observable = allClubs.compose(
          transform(drawClub, nonUserClub.newWithDraw()));
      preferences.storeLeagueClubs(observable);
    }
  }

  private Observable.Transformer<Club, Club> transform(Club clubA, Club clubB) {
    return (Observable<Club> observable) -> allClubs
        .filter((club) -> !club.name().equals(clubB.name()))
        .filter((club) -> !club.name().equals(clubA.name()))
        .mergeWith(Observable.just(clubA))
        .mergeWith(Observable.just(clubB));
  }
}
