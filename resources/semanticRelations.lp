_pos(nikola,nnp).
_pos(tesla,nnp).
_pos(be,vbd).
_pos(a,dt).
_pos(serbian,jj).
_pos(american,jj).
_pos(inventor,nn).
_pos(electrical,jj).
_pos(engineer,nn).
_pos(mechanical,jj).
_pos(engineer,nn).
_pos(physicist,nn).
_pos(and,cc).
_pos(futurist,nn).
_pos(best,rb).
_pos(know,vbn).
_pos(for,in).
_pos(he,prp_po).
_pos(contribution,nns).
_pos(to,to).
_pos(the,dt).
_pos(design,nn).
_pos(of,in).
_pos(the,dt).
_pos(modern,jj).
_pos(alternate,jj).
_pos(current,jj).
_pos(electricity,nn).
_pos(supply,nn).
_pos(system,nn).


_compound(tesla,nikola).
_nsubj(inventor,tesla).
_cop(inventor,be).
_det(inventor,a).
_amod(inventor,serbian).
_amod(inventor,american).
_amod(engineer,electrical).
_conj_and(inventor,engineer).
_amod(engineer,mechanical).
_conj_and(inventor,engineer).
_conj_and(inventor,physicist).
_cc(inventor,and).
_conj_and(inventor,futurist).
_advmod(know,best).
_acl(futurist,know).
_case(contribution,for).
_nmod_poss(contribution,he).
_nmod_for(know,contribution).
_case(design,to).
_det(design,the).
_nmod_to(contribution,design).
_case(system,of).
_det(system,the).
_amod(system,modern).
_amod(system,alternate).
_amod(system,current).
_compound(system,electricity).
_compound(system,supply).
_nmod_of(design,system).


synonym(tesla,nikola_tesla).
synonym(nikola,nikola_tesla).
is_a(electricity_system,system).
is_a(supply_system,system).


event(1, be, null, null).
event(2, know, null, null).

synonym(tesla,nikola_tesla).
synonym(nikola,nikola_tesla).
is_a(electricity_system,system).
is_a(supply_system,system).
event(1,be,null,null).
event(2,know,null,null).
value(serbian,inventor).
value(american,inventor).
value(electrical,engineer).
value(mechanical,engineer).
value(modern,system).
value(alternate,system).
value(current,system).
purpose(contribution,2).
manner(best,2).
agent(tesla,2).
is_a(tesla,inventor).
is_a(tesla,engineer).
is_a(tesla,physicist).
is_a(tesla,futurist).
part_whole(design,system).
possession(he,contribution).
