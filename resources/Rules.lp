
%`````````````````````````````````````````````````````````````POS Rules````````````````````````````````````````````````````
_verb(A) :- _pos(A,vb). 
_verb(A) :- _pos(A,vbd). 
_verb(A) :- _pos(A,vbg).
_verb(A) :- _pos(A,vbn).
_verb(A) :- _pos(A,vbp).
_verb(A) :- _pos(A,vbz).
           
_noun(A) :- _pos(A,nn).
_noun(A) :- _pos(A,nns).
_noun(A) :- _pos(A,nnp).
_noun(A) :- _pos(A,nnps).
_noun(A) :- _pos(A,prp).
_noun(A) :- _pos(A,prp_po).
_noun(A) :- _pos(A,ex).

_adjective(A) :- _pos(A,jj).
_adjective(A) :- _pos(A,jjr).
_adjective(A) :- _pos(A,jjs).

_adverb(A) :- _pos(A,rb).
_adverb(A) :- _pos(A,rbr).
_adverb(A) :- _pos(A,rbs).



%````````````````````````````````````````````````````Rules for Semantic Relation``````````````````````````````````````````````


%~~~~~~~~~~~~~~~~~~~~~~~~~~      1. Possession     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
possession(X,Y) :- event(_,V,_,_), _verb(V), _possessive_verbs(V), _dobj(V,X), _noun(X), _nsubj(V,Y), _noun(Y), not _ab_possession(X,Y).

possession(X,Y) :- _noun(X), _noun(Y), _nmod_poss(Y,X), not _ab_possession(X,Y).

%~~~~~~~~~~~~~~~~~~~~~~~~~~      2. Part-Whole/Meronymy     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
% Code in JAVA
part_whole(X,Y) :- _noun(X), _noun(Y), _nmod_of(X,Y), not _ab_part_whole(X,Y). 



%~~~~~~~~~~~~~~~~~~~~~~~~~~      3. Kinship     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

%kinship(X,Y) :- _noun(X), _noun(Y), _nmod_poss(Y,X), not _ab_kinship(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      4. Association     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

association(X,Y) :- _pos(X,nnp), _pos(Y,nnp), _conj_and(Y,X), not _ab_association(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      5. Source/Originn     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

origin(X,Y) :- _noun(X), _noun(Y), _nmod_from(Y,X), not _ab_origin(X,Y).


%~~~~~~~~~~~~~~~~~~~~~~~~~~      6. ISA      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
%Code in JAVA.
is_a(X,Y) :- _nsubj(Y,X), _det(Y,_), _cop(Y,_), not _ab_is_a(X,Y).
is_a(X,Y) :- _noun(Y), is_a(X,Z), _conj_and(Z,Y), not _ab_is_a(X,Y).


%~~~~~~~~~~~~~~~~~~~~~~~~~~      7. Synonym/Name     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
synonym(X,Y) :- _pos(X,nnp), _noun(Y), _synonym_verb(V), _xcomp(V,X), _nsubj_pass(V,Y), not _ab_synonym(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      8. Property Type     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
property_type(X,Y) :- _noun(X), _noun(Y), _nmod_of(X,Y), value(_,X), not _ab_property_type(X,Y).


%~~~~~~~~~~~~~~~~~~~~~~~~~~      9. Property/Attribute/Value     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
value(X,Y) :- _adjective(X) , _noun(Y), _amod(Y,X), not _ab_value(X,Y).
value(X,Y) :- _adjective(X), _noun(Y), _nsubj(X,Y), _cop(X,_), not _ab_value(X,Y).
value(X,Y) :- _verb(X) , _noun(Y), _amod(Y,X), not _ab_value(X,Y).


%~~~~~~~~~~~~~~~~~~~~~~~~~~      10. Quantification/Extent      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
quantification(X,Y) :- _pos(X,cd), _noun(Y), _nummod(Y,X), not _ab_quantification(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      11. Agent     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
agent(A,E) :- _noun(A), event(E,B,_,_), _verb(B), _nsubj(B,A), not _ab_nsubj(B,A).
agent(A,E) :- event(E,B,_,_), _verb(B), _noun(A), _nsubj_agent(B,A), not _ab_nsubj(B,A) .
agent(A,E) :- event(E,B,_,_), _verb(B), _noun(A), _nmod_agent(B,A), not _ab_nsubj(B,A).
agent(A,E) :- event(E,B,_,_), _verb(B), _noun(A), _acl(C,B), nsubj(C,A), not _ab_agent(A,E).
agent(A,E) :- event(E,B,_,_), _verb(B), _noun(A), _acl(C,B), _conj_and(D,C), _nsubj(D,A), not _ab_agent(A,E).

_ab_nsubj(A,B) :- _nsubj(A,B), _cop(A,_).
%~~~~~~~~~~~~~~~~~~~~~~~~~~       12. Experiencer     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

experiencer(X,Y) :- _noun(X), event(Y,B,_,_), _verb(B), _experiencing_verbs(B), _nsubj(B,X), not _ab_experiencer(X,Y).
 

%~~~~~~~~~~~~~~~~~~~~~~~~~~       13. Instrument     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
instrument(X,Y) :- _noun(X), event(Y,A,_,_), _verb(A), _nmod_with(A,X), not _ab_instrument(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~       14. Theme/Result     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
theme(X,Y) :- _verb(A), event(X,A,_,_), event(Y,B,_,_), _xcomp(B,A), not _ab_theme(X,Y).
theme(X,Y) :- _noun(X), event(Y,B,_,_), _dobj(B,X), not _ab_theme(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~       15. Recipient/Receiver     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
receiver(X,Y) :- _noun(X), event(Y,A,_,_), _verb(A), _iobj(A,X), not _ab_receiver(X,Y). 
receiver(X,Y) :- _noun(X), event(Y,A,_,_), _verb(A), _dobj(A,B), _nmod_poss(B,X), not _ab_receiver(X,Y). 




%~~~~~~~~~~~~~~~~~~~~~~~~~~       16. Topic/Content     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
topic(X,Y) :- _noun(X), event(Y,A,_,_), _verb(A), _content_verb(A), _nmod_about(A,X), not _ab_topic(X,Y).
topic(X,Y) :- event(X,A,_,_), _verb(A), event(Y,B,_,_), _verb(B), _content_verb(B), _ccomp(B,A), not _ab_topic(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~       17. Intent     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
intent(X,Y) :- _noun(Y), event(X,A,_,_), _verb(A), _verb(B), _intent_verb(B), _xcomp(B,A), _nsubj(Y,B), not _ab_intent(X,Y).
intent(X,Y) :- _noun(Y), event(X,A,_,_), _verb(A), _aux(A,B), _pos(B,md), _nsubj(A,Y), not _ab_intent(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~       18. Stimulus     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
% Perceived through senses.
stimulus(X,Y) :- event(Y,V,_,_), _stimulation_verb(V), _dobj(V,X), not _ab_stimulus(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~       19. Manner     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
manner(X,Y) :- _adverb(X), event(Y,V,_,_), _verb(V), _advmod(V,X), not _ab_manner(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~      20. At Location/Space/Direction/Source/Path/Goal     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
at_location(E,L) :- event(E,V,_,_), _place(V,L). 
_place(V,L) :- _motion_verb(V),_nmod_to(V,L), _noun(L), not _ab_place(V,L).
_place(V,L) :- _motion_verb(V),_nmod_from(V,L), _noun(L), not _ab_place(V,L).




%~~~~~~~~~~~~~~~~~~~~~~~~~~      21. At Time     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
at_time(E1,T) :- event(E1,V,_,_), _verb(V), _nmod_tmod(V,T), not _ab_at_time(E1,T).




%~~~~~~~~~~~~~~~~~~~~~~~~~~      22. Cause     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
cause(E1,E2) :- event(E1,A,_,_), event(E2,B,_,_), _verb(A), _verb(B), _advcl(B,A), not _ab_cause(E1,E2).
cause(E,Y):- event(E,A,_,_), _verb(A), _causal_verb(A), _dobj(A,Y), not _ab_cause(E,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      23. Make Produce     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
make_produce(X,Y) :- _noun(X), _noun(Y), _production_verb(Z), _dobj(Z,Y), _nsubj(Z,X), not _ab_make_produce(X,Y).


% 24. Reason/ Justification

% 25. Purpose
purpose(X,Y) :- event(Y,V,_,_), _verb(V), _nmod_for(V,X), not _ab_purpose(X,Y).
% 26. Influence




%------Facts---------
_motion_verb(go).
_motion_verb(take).
_motion_verb(travel).

_possessive_verbs(has).
_possessive_verbs(own).

_experiencing_verbs(feel).
_experiencing_verbs(see).

_content_verb(talk).
_content_verb(say).

_intent_verb(want).

_stimulation_verb(hear).
_stimulation_verb(smell).

_causal_verb(cause).

_production_verb(create).
_production_verb(manufacture).
%````````````````````````````````````````````````

%1. Subject of an event is the agent of that event.
%agent(X,E) :-  event(E,_,X,_). 

%2. An Experiencer can be considered as an agent. 
agent(X,E) :- experiencer(X,E).

%3. Hierarchical IS_A relationship
is_a(E1,X,Y) :- is_a(E1,X,Z), is_a(E2,Z,Y).
 
%4. Hierarchical part-whole relationship
part_whole(E1,X,Y) :- part_whole(E1,X,Z), part_whole(E2,Z,Y).

%5. X is the effect of Y. If Y is the cause of X.
effect(E1,E2) :- cause(E2,E1).
