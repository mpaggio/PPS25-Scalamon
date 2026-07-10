package scalamon.logics.state

import scalamon.domain.weather.Weather
import scalamon.domain.weather.Weather.HeavySunlight
import scalamon.logics.state.StateTransformerModuleImpl.*

import scala.collection.immutable.ListMap

object Prova:

  final case class Lens[S, A](get: S => A, set: (S, A) => S):
    def apply(f: A => A): S => S = s => set(s, f(get(s)))

    infix def andThen[B](l: Lens[A, B]): Lens[S, B] =
      Lens(s => l.get(get(s)), (s, b) => set(s, l.set(get(s), b)))

  final case class Traversal[S, A](modify: (A => A) => S => S):
    infix def andThen[B](t: Traversal[A, B]): Traversal[S, B] =
      Traversal(f => modify(t.modify(f)))

  extension [S, A](l: Lens[S, A])
    def each: Traversal[S, A] = Traversal(f => l.apply(f))
    infix def andThenT[B](t: Traversal[A, B]): Traversal[S, B] = Traversal(f => l.apply(t.modify(f)))

  object BattleStateImpl:
    val selfL: Lens[Bs, PlayerState] = Lens (_.self, (bs, p) => bs.copy (self = p) )
    val opponentL: Lens[Bs, PlayerState] = Lens (_.opponent, (bs, p) => bs.copy (opponent = p) )
    val weatherL: Lens[Bs, Weather] = Lens (_.weather, (bs, w) => bs.copy (weather = w) )

  object PlayerStateModuleImpl:
    val activeL: Lens[Ps, PokemonState] =
      Lens (_.getActive, (ps, pk) => ps.copy (team = ps.team.updated (ps.activeId, pk) ) )
    val benchT: Traversal[Ps, PokemonState] =
      Traversal (f => ps => ps.copy (team = ps.team.map ((k, v) => (k, if k == ps.activeId then v else f (v) ) ) ) )
    def teamThat(p: PokemonState => Boolean): Traversal[Ps, PokemonState] =
      Traversal(f => ps => ps.copy(team = ps.team.view.mapValues(s => if p(s) then f(s) else s).toMap))

  object PokemonStateModuleImpl:
    val hpL: Lens[Pks, Stat] = Lens (_.currentHp, (pk, h) => pk.copy (currentHp = h.clamped (0, pk.maxHp) ) )
    val statsL: Lens[Pks, StatsState] = Lens (_.modifiedStats, (pk, s) => pk.copy (modifiedStats = s) )


  final case class HookKey(owner: String, name: String, priority: Int = 0)

  type Behavior[R, A, S] = (R, A) => S => S
  type Mid[R, A, S] = Behavior[R, A, S] => Behavior[R, A, S]

  final case class Hooked[R, A, S](base: Behavior[R, A, S], hooks: ListMap[HookKey, Mid[R, A, S]] = ListMap.empty):
    def resolve(r: R, a: A): S => S =
      hooks.toList.sortBy(_._1.priority).map(_._2)
        .foldRight(base)((mid, acc) => mid(acc))(r, a)

    def add(k: HookKey, m: Mid[R, A, S]): Hooked[R, A, S] = copy(hooks = hooks.updated(k, m))

    def removeOwned(owner: String): Hooked[R, A, S] = copy(hooks = hooks.filter(_._1.owner != owner))


  def intercepted[A, S](target: Lens[BattleState, S], slot: Lens[S, Hooked[BattleState, A, S]])(a: A): StateTransformer =
    bs => target.apply(s => slot.get(s).resolve(bs, a)(s))(bs)

  def install[A, S](target: Lens[BattleState, S], slot: Lens[S, Hooked[BattleState, A, S]])
                   (k: HookKey, m: Mid[BattleState, A, S]): StateTransformer =
    (target andThen slot).apply(_.add(k, m))

  def uninstallOwned[A, S](target: Lens[BattleState, S], slot: Lens[S, Hooked[BattleState, A, S]])(owner: String): StateTransformer =
    (target andThen slot).apply(_.removeOwned(owner))

/*
    case class Ps(team:
  switchSlot: Hooked[BattleState, PokemonId, Ps]
  = Hooked((_, id) => ps => ps.copy(activeId = id))
  )

  val switchSlotL: Lens[Ps, Hooked[BattleState, PokemonId, Ps]] =
    Lens(_.switchSlot, (ps, h) => ps.copy(switchSlot = h))

  // l'operazione pubblica per Self:
  def switchSelf(id: PokemonId): StateTransformer = intercepted(selfL, switchSlotL)(id)
    
    
    
    


    case class Installation(run: String => StateTransformer) // riceve l'owner

    case class AbilityDef(name: String, installs: List[Installation])
  
    
    import BattleStateImpl.*
    val shadowTag = AbilityDef("ShadowTag", List(
      Installation(owner => install(opponentL, switchSlotL)(HookKey(owner, "block"), _ => (_, _) => identity))
    ))

    val regenerator = AbilityDef("Regenerator", List(
      Installation(owner => install(selfL, switchSlotL)(
        HookKey(owner, "heal-on-exit"),
        base => (bs, id) => activeL.modify(hpL.modify(_ + 20)).andThen(base(bs, id))
      ))
    ))

    val drought = AbilityDef("Drought", List(
      Installation(owner => weatherL.apply(_ => HeavySunlight))
    ))


*/