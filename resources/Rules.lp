
%`````````````````````````````````````````````````````````````POS Rules````````````````````````````````````````````````````
verb(A) :- pos(A,vb). 
verb(A) :- pos(A,vbd). 
verb(A) :- pos(A,vbg).
verb(A) :- pos(A,vbn).
verb(A) :- pos(A,vbp).
verb(A) :- pos(A,vbz).

noun(A) :- pos(A,nn).
noun(A) :- pos(A,nns).
noun(A) :- pos(A,nnp).
noun(A) :- pos(A,nnps).
noun(A) :- pos(A,prp).
noun(A) :- pos(A,prp_po).
noun(A) :- pos(A,ex).

adjective(A) :- pos(A,jj).
adjective(A) :- pos(A,jjr).
adjective(A) :- pos(A,jjs).

adverb(A) :- pos(A,rb).
adverb(A) :- pos(A,rbr).
adverb(A) :- pos(A,rbs).



%````````````````````````````````````````````````````Rules for Semantic Relation``````````````````````````````````````````````


%~~~~~~~~~~~~~~~~~~~~~~~~~~      1. Possession     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
possession(X,Y) :- event(_,V,_,_), verb(V), possessive_verbs(V), dobj(V,X), noun(X), nsubj(V,Y), noun(Y), not ab_possession(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      2. Part-Whole/Meronymy     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
% Code in JAVA
part_whole(X,Y) :- noun(X), noun(Y), nmod_of(X,Y), not ab_part_whole(X,Y). 



%~~~~~~~~~~~~~~~~~~~~~~~~~~      3. Kinship     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

kinship(X,Y) :- noun(X), noun(Y), nmod_poss(Y,X), not ab_kinship(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      4. Association     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

association(X,Y) :- pos(X,nnp), pos(Y,nnp), conj_and(Y,X), not ab_association(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      5. Source/Originn     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

origin(X,Y) :- noun(X), noun(Y), nmod_from(Y,X), not ab_origin(X,Y).


%~~~~~~~~~~~~~~~~~~~~~~~~~~      6. ISA      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
%Code in JAVA.
is_a(X,Y) :- nsubj(Y,X), det(Y,_), cop(Y,_), not ab_is_a(X,Y).
is_a(X,Y) :- noun(Y), is_a(X,Z), conj_and(Z,Y), not ab_is_a(X,Y).


%~~~~~~~~~~~~~~~~~~~~~~~~~~      7. Synonym/Name     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
synonym(X,Y) :- pos(X,nnp), noun(Y), synonym_verb(V), xcomp(V,X), nsubj_pass(V,Y), not ab_synonym(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      8. Property Type     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
property_type(X,Y) :- noun(X), noun(Y), nmod_of(X,Y), value(_,X), not ab_property_type(X,Y).


%~~~~~~~~~~~~~~~~~~~~~~~~~~      9. Property/Attribute/Value     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
value(X,Y) :- adjective(X) , noun(Y), amod(Y,X), not ab_value(X,Y).
value(X,Y) :- adjective(X), noun(Y), nsubj(X,Y), cop(X,_), not ab_value(X,Y).
value(X,Y) :- verb(X) , noun(Y), amod(Y,X), not ab_value(X,Y).


%~~~~~~~~~~~~~~~~~~~~~~~~~~      10. Quantification/Extent      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
quantification(X,Y) :- pos(X,cd), noun(Y), nummod(Y,X), not ab_quantification(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      11. Agent     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
agent(A,E) :- noun(A), event(E,B,_,_), verb(B), nsubj(B,A), not ab_nsubj(B,A).
ab_nsubj(A,B) :- nsubj(A,B), cop(A,_).
agent(A,E) :- event(E,B,_,_), verb(B), noun(A), nsubj_agent(B,A) .
agent(A,E) :- event(E,B,_,_), verb(B), noun(A), nmod_agent(B,A) .


%~~~~~~~~~~~~~~~~~~~~~~~~~~       12. Experiencer     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
%
experiencer(X,Y) :- noun(X), event(Y,B,_,_), verb(B), experiencing_verbs(B), nsubj(B,X), not ab_experiencer(X,Y).
 

%~~~~~~~~~~~~~~~~~~~~~~~~~~       13. Instrument     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ 
instrument(X,Y) :- noun(X), event(Y,A,_,_), verb(A), nmod_with(A,X), not ab_instrument(X,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~       14. Theme/Result     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
theme(X,Y) :- verb(A), event(X,A,_,_), event(Y,B,_,_), xcomp(B,A), not ab_theme(X,Y).
theme(X,Y) :- noun(X), event(Y,B,_,_), dobj(B,X), not ab_theme(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~       15. Recipient/Receiver     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
receiver(X,Y) :- noun(X), event(Y,A,_,_), verb(A), iobj(A,X), not ab_receiver(X,Y). 
receiver(X,Y) :- noun(X), event(Y,A,_,_), verb(A), dobj(A,B), nmod_poss(B,X), not ab_receiver(X,Y). 




%~~~~~~~~~~~~~~~~~~~~~~~~~~       16. Topic/Content     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
topic(X,Y) :- noun(X), event(Y,A,_,_), verb(A), content_verb(A), nmod_about(A,X), not ab_topic(X,Y).
topic(X,Y) :- event(X,A,_,_), verb(A), event(Y,B,_,_), verb(B), content_verb(B), ccomp(B,A), not ab_topic(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~       17. Intent     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
intent(X,Y) :- noun(Y), event(X,A,_,_), verb(A), verb(B), intent_verb(B), xcomp(B,A), nsubj(Y,B), not ab_intent(X,Y).
intent(X,Y) :- noun(Y), event(X,A,_,_), verb(A), aux(A,B), pos(B,md), nsubj(A,Y), not ab_intent(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~       18. Stimulus     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
% Perceived through senses.
stimulus(X,Y) :- event(Y,V,_,_), stimulation_verb(V), dobj(V,X), not ab_stimulus(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~       19. Manner     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
manner(X,Y) :- adverb(X), event(Y,V,_,_), verb(V), advmod(V,X), not ab_manner(X,Y).




%~~~~~~~~~~~~~~~~~~~~~~~~~~      20. At Location/Space/Direction/Source/Path/Goal     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
at_location(E,L) :- event(E,V,_,_), place(V,L). 
place(V,L) :- motion_verb(V),nmod_to(V,L), noun(L), not ab_place(V,L).
place(V,L) :- motion_verb(V),nmod_from(V,L), noun(L), not ab_place(V,L).




%~~~~~~~~~~~~~~~~~~~~~~~~~~      21. At Time     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
at_time(E1,T) :- event(E1,V,_,_), verb(V), nmod_tmod(V,T).




%~~~~~~~~~~~~~~~~~~~~~~~~~~      22. Cause     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
cause(E1,E2) :- event(E1,A,_,_), event(E2,B,_,_), verb(A), verb(B), advcl(B,A), not ab_cause(E1,E2).
cause(E,Y):- event(E,A,_,_), verb(A), causal_verb(A), dobj(A,Y), not ab_cause(E,Y).



%~~~~~~~~~~~~~~~~~~~~~~~~~~      23. Make Produce     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
make_produce(X,Y) :- noun(X),noun(Y), verb(Z), dobj(Z,Y), nsubj(Z,X), not ab_make_produce(X,Y).


% 24. Reason/ Justification

% 25. Purpose
purpose(X,Y) :- event(Y,V,_,_), verb(V), nmod_for(V,X), not ab_purpose(X,Y).
% 26. Influence




%------Facts---------
motion_verb(went).
motion_verb(taking).
motion_verb(traveling).

possessive_verbs(has).
possessive_verbs(own).

experiencing_verbs(feels).
experiencing_verbs(saw).

content_verb(talked).
content_verb(said).

intent_verb(want).

stimulation_verb(heard).
stimulation_verb(smell).

causal_verb(cause).
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
