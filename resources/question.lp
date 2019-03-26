question('when was nikola _ tesla born ? .', 1).
question('when was nikola _ tesla born ? .', certain, X2) :- _mod(tesla, _),_mod(tesla, nikola),_property(E2, bear, _by, S2),_similar(tesla, O2),event(E2, bear, _, O2),time(X2).
question('when was nikola _ tesla born ? .', certain, X2) :- _mod(tesla, _),_mod(tesla, nikola),_similar(tesla, O2),event(E2, bear, S2, O2),time(X2).
question('when was nikola _ tesla born ? .', likely, X2) :- _property(E2, bear, _by, S2),_similar(tesla, O2),event(E2, bear, _, O2),time(X2).
question('when was nikola _ tesla born ? .', likely, X2) :- _similar(tesla, O2),event(E2, bear, S2, O2),time(X2).
question('when was nikola _ tesla born ? .', possible, X2) :- time(X2).
?- query(1,Question,answer( Answer, Confidence_Level)).