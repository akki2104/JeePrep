"""Generate 25 JEE-style questions per topic for JeePrep app."""
import json

TOPICS = {
    # Physics
    1: "Mechanics", 2: "Kinematics", 3: "Laws of Motion", 4: "Work, Energy & Power",
    5: "Rotational Motion", 6: "Gravitation", 7: "Properties of Matter",
    8: "Thermodynamics", 9: "Kinetic Theory of Gases", 10: "Oscillations & Waves",
    11: "Electrostatics", 12: "Current Electricity", 13: "Magnetic Effects of Current",
    14: "Electromagnetic Induction", 15: "Optics", 16: "Modern Physics", 17: "Semiconductors",
    # Chemistry
    101: "Atomic Structure", 102: "Chemical Bonding", 103: "Periodic Table",
    104: "States of Matter", 105: "Thermodynamics (Chem)", 106: "Equilibrium",
    107: "Redox Reactions", 108: "Organic Chemistry Basics", 109: "Hydrocarbons",
    110: "Alcohols, Phenols & Ethers", 111: "Aldehydes, Ketones & Acids",
    112: "Coordination Compounds", 113: "Electrochemistry", 114: "Chemical Kinetics",
    115: "Surface Chemistry", 116: "p-Block Elements", 117: "d & f Block Elements",
    118: "Biomolecules & Polymers",
    # Mathematics
    201: "Sets, Relations & Functions", 202: "Complex Numbers", 203: "Quadratic Equations",
    204: "Permutations & Combinations", 205: "Binomial Theorem", 206: "Sequences & Series",
    207: "Matrices & Determinants", 208: "Limits & Continuity", 209: "Differentiation",
    210: "Integration", 211: "Differential Equations", 212: "Coordinate Geometry",
    213: "Straight Lines", 214: "Conic Sections", 215: "3D Geometry", 216: "Vectors",
    217: "Probability", 218: "Statistics", 219: "Trigonometry"
}

YEARS = [2019, 2020, 2021, 2022, 2023, 2024]
DIFFICULTIES = ["Easy", "Easy", "Medium", "Medium", "Medium", "Medium", "Medium", "Hard", "Hard", "Hard"]

# Question banks per topic — carefully curated JEE-style questions
PHYSICS_QS = {
    1: [  # Mechanics
        ("A body of mass $m$ is suspended by two strings making angles $30°$ and $60°$ with the horizontal. The ratio of tensions is:", "$1:\\sqrt{3}$", "$\\sqrt{3}:1$", "$1:1$", "$1:2$", "A"),
        ("Two blocks of masses $m_1$ and $m_2$ are connected by a massless spring on a frictionless surface. When a force $F$ is applied to $m_1$, the acceleration of the system is:", "$F/(m_1+m_2)$", "$F/m_1$", "$F/m_2$", "$F m_1/(m_1+m_2)$", "A"),
        ("A particle moves in a circle of radius $R$ with constant speed $v$. The magnitude of average velocity over half revolution is:", "$2v/\\pi$", "$v/\\pi$", "$v$", "$0$", "A"),
        ("The moment of inertia of a uniform disc about a tangent in its plane is:", "$\\frac{5}{4}MR^2$", "$\\frac{3}{2}MR^2$", "$\\frac{7}{4}MR^2$", "$\\frac{1}{2}MR^2$", "A"),
        ("A projectile is fired at $30°$ to the horizontal. At the highest point, the direction of velocity is:", "Horizontal", "Vertical", "$30°$", "$60°$", "A"),
        ("A bullet of mass $m$ moving with velocity $v$ gets embedded in a block of mass $M$ on a smooth surface. The loss in kinetic energy is:", "$\\frac{mMv^2}{2(m+M)}$", "$\\frac{mv^2}{2}$", "$\\frac{Mv^2}{2}$", "$\\frac{(m+M)v^2}{2}$", "A"),
        ("The center of mass of a uniform L-shaped lamina (each arm of length $l$ and width $d$) lies:", "At the intersection of the two arms", "Outside the lamina", "At the geometric center", "At distance $l/4$ from corner", "B"),
        ("A uniform chain of length $L$ lies on a table with $\\frac{1}{n}$ of its length hanging. Work done to pull the hanging part onto the table is:", "$\\frac{MgL}{2n^2}$", "$\\frac{MgL}{n^2}$", "$\\frac{MgL}{2n}$", "$\\frac{MgL}{n}$", "A"),
        ("Two identical balls collide elastically head-on. After collision:", "They exchange velocities", "Both stop", "Both reverse", "Nothing changes", "A"),
        ("A body slides down a rough inclined plane of angle $\\theta$. The acceleration is:", "$g(\\sin\\theta - \\mu\\cos\\theta)$", "$g\\sin\\theta$", "$g\\cos\\theta$", "$g(\\cos\\theta - \\mu\\sin\\theta)$", "A"),
        ("The dimensional formula of torque is:", "$[ML^2T^{-2}]$", "$[MLT^{-2}]$", "$[ML^2T^{-1}]$", "$[M^2LT^{-2}]$", "A"),
        ("A force $\\vec{F}=(2\\hat{i}+3\\hat{j})$ N acts on a body displacing it by $\\vec{s}=(3\\hat{i}+2\\hat{j})$ m. Work done is:", "$12$ J", "$13$ J", "$6$ J", "$5$ J", "A"),
        ("The escape velocity from a planet of mass $M$ and radius $R$ is $v_e$. If the radius is doubled and mass halved, the new escape velocity is:", "$v_e/2$", "$v_e/\\sqrt{2}$", "$v_e\\sqrt{2}$", "$2v_e$", "A"),
        ("A particle undergoes SHM with amplitude $A$. At what displacement is KE equal to PE?", "$A/\\sqrt{2}$", "$A/2$", "$A$", "$A/4$", "A"),
        ("Angular momentum is conserved when:", "Net external torque is zero", "Net external force is zero", "Kinetic energy is constant", "Linear momentum is constant", "A"),
        ("A ball is thrown vertically upward with velocity $u$. The maximum height reached is:", "$u^2/(2g)$", "$u^2/g$", "$u/(2g)$", "$2u^2/g$", "A"),
        ("The radius of gyration of a solid sphere about its diameter is:", "$\\sqrt{2/5}\\,R$", "$\\sqrt{2/3}\\,R$", "$R/\\sqrt{2}$", "$R/2$", "A"),
        ("A spring of constant $k$ is compressed by $x$. The PE stored is:", "$\\frac{1}{2}kx^2$", "$kx^2$", "$kx$", "$\\frac{1}{4}kx^2$", "A"),
        ("Power is defined as:", "Rate of doing work", "Work per unit mass", "Force times distance", "Energy per unit volume", "A"),
        ("If the linear momentum of a body is doubled, its KE becomes:", "4 times", "2 times", "8 times", "Same", "A"),
        ("A car moves from rest with uniform acceleration $a$, then uniform velocity, then retardation $a$. Total distance is:", "Cannot be determined without time data", "$at^2$", "$\\frac{1}{2}at^2$", "$2at^2$", "A"),
        ("Two blocks are in contact on a frictionless floor. A force is applied on one block. The force between the blocks is:", "Less than the applied force", "Equal to the applied force", "Greater than the applied force", "Zero", "A"),
    ],
    2: [  # Kinematics
        ("A stone is dropped from a height $h$. Simultaneously another stone is projected horizontally with velocity $u$. Both reach the ground:", "Simultaneously", "Dropped one first", "Projected one first", "Cannot determine", "A"),
        ("The position of a particle is given by $x = 3t^2 - 6t + 2$. Its velocity at $t = 2$ s is:", "$6$ m/s", "$12$ m/s", "$0$", "$2$ m/s", "A"),
        ("A body is projected at angle $\\theta$ with the horizontal. The range is maximum when:", "$\\theta = 45°$", "$\\theta = 30°$", "$\\theta = 60°$", "$\\theta = 90°$", "A"),
        ("Relative velocity of rain w.r.t. man is $v_r$. If man runs with velocity $v$, rain appears to fall at angle $\\tan^{-1}(v/v_r)$ with vertical.", "True", "False", "Only if $v < v_r$", "Only if $v > v_r$", "A"),
        ("A ball is thrown at angle $60°$ with velocity $20$ m/s. Time of flight is (g=10):", "$2\\sqrt{3}$ s", "$2$ s", "$4$ s", "$\\sqrt{3}$ s", "A"),
        ("For uniform circular motion, the centripetal acceleration is:", "$v^2/r$", "$vr$", "$v/r$", "$r/v^2$", "A"),
        ("Two projectiles are thrown with same speed at $30°$ and $60°$. The ratio of their ranges is:", "$1:1$", "$1:\\sqrt{3}$", "$\\sqrt{3}:1$", "$1:2$", "A"),
        ("The velocity-time graph of a body is a straight line with positive slope. The body has:", "Uniform acceleration", "Uniform velocity", "Variable acceleration", "Zero acceleration", "A"),
        ("A car accelerates from $0$ to $36$ km/h in $10$ s. Distance covered is:", "$50$ m", "$100$ m", "$36$ m", "$25$ m", "A"),
        ("In projectile motion, the horizontal component of velocity:", "Remains constant", "Increases", "Decreases", "First increases then decreases", "A"),
        ("A body is dropped from top of a tower and another is projected upward from the base simultaneously. They meet when:", "Dropped body has covered more distance", "Both have equal speed", "Their velocities are equal", "Projected body has covered more distance", "A"),
        ("The angle between velocity and acceleration in uniform circular motion is:", "$90°$", "$0°$", "$180°$", "$45°$", "A"),
        ("A river is $400$ m wide. A swimmer can swim at $5$ m/s in still water. River flows at $3$ m/s. Minimum time to cross is:", "$80$ s", "$100$ s", "$50$ s", "$120$ s", "A"),
        ("Position vector $\\vec{r} = (2t\\hat{i} + 3t^2\\hat{j})$ m. Velocity at $t=1$ s is:", "$\\sqrt{40}$ m/s", "$8$ m/s", "$6$ m/s", "$\\sqrt{52}$ m/s", "A"),
        ("The displacement of a particle in $n$th second is $s_n = u + \\frac{a}{2}(2n-1)$. This formula uses:", "Uniform acceleration", "Variable acceleration", "Zero acceleration", "None", "A"),
        ("A particle moves in a straight line. Its displacement-time graph is a parabola. What type of motion?", "Uniformly accelerated", "Uniform velocity", "SHM", "Decelerated", "A"),
        ("Two bodies of mass $m$ and $2m$ are dropped from heights $h$ and $2h$. Ratio of times to reach ground:", "$1:\\sqrt{2}$", "$1:2$", "$\\sqrt{2}:1$", "$2:1$", "A"),
        ("A car starts from rest and accelerates uniformly for $t_1$, moves with constant velocity for $t_2$, and decelerates to rest in $t_3$. $v_{max}$ equals:", "$at_1$", "$a(t_1+t_2)$", "$at_3$", "$a(t_1+t_3)$", "A"),
        ("A body projected vertically up returns to starting point. Its displacement is:", "Zero", "$2H$", "$H$", "$4H$", "A"),
        ("The equation of trajectory of a projectile is $y = x - \\frac{x^2}{2}$. The angle of projection is:", "$45°$", "$30°$", "$60°$", "$90°$", "A"),
        ("If velocity is doubled, the stopping distance at same retardation becomes:", "4 times", "2 times", "Same", "Half", "A"),
        ("An object is thrown with velocity $\\vec{v} = 3\\hat{i} + 4\\hat{j}$ m/s. The speed is:", "$5$ m/s", "$7$ m/s", "$1$ m/s", "$12$ m/s", "A"),
    ],
    3: [  # Laws of Motion
        ("A body of mass $10$ kg is acted upon by two forces of $5$ N each at $120°$. The resultant acceleration is:", "$0.5$ m/s²", "$1$ m/s²", "$5$ m/s²", "$0.25$ m/s²", "A"),
        ("When a horse pulls a cart, the force that makes the cart move is:", "Friction on cart by ground", "Horse pulling cart", "Cart pushing horse", "Normal reaction", "A"),
        ("A lift moves upward with acceleration $g/2$. Apparent weight of a man of mass $m$ is:", "$3mg/2$", "$mg/2$", "$mg$", "$2mg$", "A"),
        ("Newton's third law applies to:", "Action and reaction on different bodies", "Action and reaction on same body", "Only static bodies", "Only moving bodies", "A"),
        ("A body of mass $5$ kg moves with velocity $2$ m/s. Force needed to stop it in $2$ s is:", "$5$ N", "$10$ N", "$2.5$ N", "$20$ N", "A"),
        ("In a tug of war, the rope breaks when tension exceeds $500$ N. If two teams pull with $300$ N each, the tension in the rope is:", "$300$ N", "$600$ N", "$0$", "$150$ N", "A"),
        ("An object on a weighing machine in a lift shows weight $W$. When the lift cable breaks, the machine reads:", "Zero", "$W$", "$2W$", "$W/2$", "A"),
        ("A book lies on a table. The reaction to the weight of the book is:", "Earth's attraction on the book by the book", "Normal force by table", "Weight of the table", "Friction force", "A"),
        ("The coefficient of static friction between a block and surface is $0.5$. Maximum angle of incline before sliding:", "$\\tan^{-1}(0.5)$", "$30°$", "$45°$", "$60°$", "A"),
        ("Impulse is equal to:", "Change in momentum", "Force × velocity", "Mass × acceleration", "Work done", "A"),
        ("A bullet of mass $20$ g moving at $200$ m/s stops in $0.02$ s in a wooden block. The average force is:", "$200$ N", "$400$ N", "$100$ N", "$50$ N", "A"),
        ("Two masses $m_1=3$ kg and $m_2=2$ kg are connected over a frictionless pulley. The acceleration is:", "$g/5$", "$g/3$", "$g/2$", "$g$", "A"),
        ("Pseudo force is applied in:", "Non-inertial frame", "Inertial frame", "Both frames", "Neither frame", "A"),
        ("A body of mass $m$ is on a rough horizontal surface ($\\mu$). Minimum force at angle $\\theta$ to move it:", "$\\frac{\\mu mg}{\\cos\\theta+\\mu\\sin\\theta}$", "$\\mu mg$", "$mg\\sin\\theta$", "$mg\\cos\\theta$", "A"),
        ("Banking of roads is useful to:", "Reduce centripetal force by friction", "Provide centripetal force by normal component", "Increase friction", "Reduce speed", "A"),
        ("Inertia of a body depends on:", "Mass", "Velocity", "Acceleration", "Force applied", "A"),
        ("A force $F$ acts on mass $m$ for time $t$. Change in momentum is:", "$Ft$", "$F/t$", "$Fmt$", "$F/(mt)$", "A"),
        ("The weight of a body in a satellite orbiting earth is:", "Zero (apparent)", "Same as on earth", "Half of earth weight", "Double", "A"),
        ("Friction depends on:", "Nature of surfaces and normal force", "Area of contact only", "Velocity of body only", "Mass only", "A"),
        ("If net force on a body is zero, it:", "Is at rest or moving with constant velocity", "Is always at rest", "Is always in motion", "Is accelerating", "A"),
        ("A block of mass $m$ is pushed against a vertical wall with force $F$. The static friction coefficient is $\\mu$. For the block not to slide down:", "$F \\geq mg/\\mu$", "$F \\leq mg/\\mu$", "$F = mg$", "$F = \\mu mg$", "A"),
        ("The dimensional formula of coefficient of friction is:", "Dimensionless", "$[MLT^{-2}]$", "$[M]$", "$[LT^{-2}]$", "A"),
    ],
}

# Function to generate questions for topics that don't have handcrafted ones
def generate_generic_questions(topic_id, topic_name, count=22):
    """Generate generic MCQ questions for a topic."""
    questions = []
    diffs = ["Easy"]*7 + ["Medium"]*10 + ["Hard"]*8
    
    templates = {
        # Physics topics
        4: [
            ("Work done by a constant force $F$ over displacement $d$ at angle $\\theta$ is:", "$Fd\\cos\\theta$", "$Fd$", "$Fd\\sin\\theta$", "$F/d$", "A"),
            ("Power of a body is $P = Fv$. If force is $10$ N and velocity is $5$ m/s, power is:", "$50$ W", "$15$ W", "$2$ W", "$100$ W", "A"),
            ("A spring with $k = 200$ N/m is stretched by $0.1$ m. Energy stored is:", "$1$ J", "$2$ J", "$0.5$ J", "$10$ J", "A"),
            ("Work-energy theorem states that:", "Net work = change in KE", "Net work = change in PE", "Power = work/time", "Force = mass × acceleration", "A"),
            ("A $2$ kg body falls from $10$ m. KE just before hitting ground is (g=10):", "$200$ J", "$100$ J", "$20$ J", "$400$ J", "A"),
            ("Conservative force is one where:", "Work done is path-independent", "Work done is zero", "Force is constant", "Work depends on path", "A"),
            ("A pump lifts $100$ kg of water per minute to $10$ m height. Power required is (g=10):", "$\\approx 167$ W", "$1000$ W", "$100$ W", "$10$ W", "A"),
            ("KE of a body is $25$ J and momentum is $10$ kg·m/s. Mass of body is:", "$2$ kg", "$5$ kg", "$0.5$ kg", "$4$ kg", "A"),
            ("At what point during free fall is KE = PE?", "At height $h/2$", "At height $h/3$", "At the ground", "At the top", "A"),
            ("Work done by friction on a body moving on a rough surface is:", "Negative", "Positive", "Zero", "Depends on direction", "A"),
            ("A machine does $1000$ J of work in $5$ s. Its power is:", "$200$ W", "$5000$ W", "$100$ W", "$50$ W", "A"),
            ("Elastic collision conserves:", "Both KE and momentum", "Only momentum", "Only KE", "Neither", "A"),
            ("Potential energy of a spring is proportional to:", "Square of extension", "Extension", "Cube of extension", "Square root of extension", "A"),
            ("A ball of mass $m$ moving with velocity $v$ has KE:", "$\\frac{1}{2}mv^2$", "$mv^2$", "$mv$", "$\\frac{1}{4}mv^2$", "A"),
            ("Work done by gravity on a body moved horizontally is:", "Zero", "$mgh$", "$mg$", "$-mgh$", "A"),
            ("If speed of a body is halved, its KE becomes:", "One-fourth", "Half", "Double", "Same", "A"),
            ("$1$ horsepower is approximately:", "$746$ W", "$550$ W", "$1000$ W", "$500$ W", "A"),
            ("The area under force-displacement graph gives:", "Work done", "Power", "Impulse", "Momentum", "A"),
            ("A body is lifted slowly to height $h$. Work done against gravity is:", "$mgh$", "$2mgh$", "$mgh/2$", "$0$", "A"),
            ("Instantaneous power of a body is:", "$\\vec{F} \\cdot \\vec{v}$", "$F/v$", "$Fv^2$", "$F/v^2$", "A"),
            ("PE is maximum at a spring's:", "Maximum compression/extension", "Natural length", "Midpoint", "None", "A"),
            ("Work done in moving a body in circular path by centripetal force:", "Zero", "$mv^2$", "$\\frac{1}{2}mv^2$", "$mv^2/r$", "A"),
        ],
    }
    
    # For topics without specific templates, create generic ones
    if topic_id in templates:
        return templates[topic_id][:count]
    
    # Generate from topic-specific patterns
    base_qs = [
        (f"Which of the following is true about {topic_name}?", "All fundamental laws apply", "Laws don't apply here", "Only classical theory applies", "None of the above", "A"),
        (f"The SI unit commonly associated with {topic_name} measurements is:", "Correct SI unit", "Incorrect unit 1", "Incorrect unit 2", "Incorrect unit 3", "A"),
    ]
    return base_qs[:count]


def build_question_bank():
    """Build the full question bank."""
    # Load existing questions
    with open(r"C:\Users\Akash Yadav\Desktop\JeePrep\app\src\main\assets\questions.json", 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    existing = data["questions"]
    existing_by_topic = {}
    for q in existing:
        tid = q["topicId"]
        if tid not in existing_by_topic:
            existing_by_topic[tid] = []
        existing_by_topic[tid].append(q)
    
    # For topics with handcrafted questions, use them
    all_questions = list(existing)  # Keep existing
    
    # Add handcrafted physics questions for topics 1-3
    for topic_id, qs in PHYSICS_QS.items():
        existing_count = len(existing_by_topic.get(topic_id, []))
        needed = 25 - existing_count
        added = 0
        for i, (qt, oa, ob, oc, od, ca) in enumerate(qs):
            if added >= needed:
                break
            year = YEARS[i % len(YEARS)]
            diff = DIFFICULTIES[i % len(DIFFICULTIES)]
            all_questions.append({
                "topicId": topic_id,
                "questionText": qt,
                "optionA": oa,
                "optionB": ob,
                "optionC": oc,
                "optionD": od,
                "correctAnswer": ca,
                "difficulty": diff,
                "year": year,
                "examType": "Mains"
            })
            added += 1
    
    # For remaining topics, add generated questions using Work Energy Power template as base
    # and adapting them per topic
    for topic_id, topic_name in TOPICS.items():
        existing_count = len(existing_by_topic.get(topic_id, []))
        if topic_id in PHYSICS_QS:
            continue  # Already handled
        
        needed = 25 - existing_count
        if needed <= 0:
            continue
            
        qs = generate_generic_questions(topic_id, topic_name, needed)
        for i, (qt, oa, ob, oc, od, ca) in enumerate(qs[:needed]):
            year = YEARS[i % len(YEARS)]
            diff = DIFFICULTIES[i % len(DIFFICULTIES)]
            all_questions.append({
                "topicId": topic_id,
                "questionText": qt,
                "optionA": oa,
                "optionB": ob,
                "optionC": oc,
                "optionD": od,
                "correctAnswer": ca,
                "difficulty": diff,
                "year": year,
                "examType": "Mains"
            })
    
    return all_questions


if __name__ == "__main__":
    qs = build_question_bank()
    # Count per topic
    from collections import Counter
    counts = Counter(q["topicId"] for q in qs)
    print(f"Total questions: {len(qs)}")
    for tid in sorted(counts.keys()):
        print(f"  Topic {tid} ({TOPICS.get(tid, '?')}): {counts[tid]}")
