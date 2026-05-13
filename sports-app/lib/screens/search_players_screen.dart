import 'package:flutter/material.dart';
import '../components/app_background.dart';
import '../models/player.dart';
import '../services/PlayerServices.dart';
import 'DataManagement/tabs/players/footballer_details_screen.dart';

class SearchPlayersScreen extends StatefulWidget {
  const SearchPlayersScreen({Key? key}) : super(key: key);

  @override
  State<SearchPlayersScreen> createState() => _SearchPlayersScreenState();
}

class _SearchPlayersScreenState extends State<SearchPlayersScreen> {
  final PlayerService _playerService = PlayerService();
  final TextEditingController _searchController = TextEditingController();

  List<Player> _players = [];
  bool _isLoading = false;
  String _error = '';

  Future<void> _searchPlayers(String name) async {
    if (name.trim().isEmpty) {
      setState(() {
        _players = [];
        _error = '';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _error = '';
    });

    try {
      final players = await _playerService.searchPlayers(name);
      setState(() {
        _players = players;
      });
    } catch (e) {
      setState(() {
        _error = '      ';
      });
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AppBackground(child: Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
    elevation: 0,
        backgroundColor: Colors.transparent,
        title: TextField(
          controller: _searchController,
          autofocus: true,
          textInputAction: TextInputAction.search,
          decoration: const InputDecoration(
            hintText: '  ',
            border: InputBorder.none,
            hintStyle: TextStyle(color: Colors.white70),
          ),
          style: const TextStyle(color: Colors.white, fontSize: 18),
          onChanged: (value) => _searchPlayers(value),
        ),),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator(color: Colors.teal))
          : _error.isNotEmpty
          ? Center(child: Text(_error))
          : _players.isEmpty
          ? const Center(
        child: Text(
          '       ',
          style: TextStyle(fontSize: 16),
        ),
      )
          : ListView.builder(
        itemCount: _players.length,
        itemBuilder: (context, index) {
          final player = _players[index];
          return Card(
            margin: const EdgeInsets.symmetric(
                horizontal: 10, vertical: 6),
            elevation: 2,
            shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10)),
            child: ListTile(
              leading: CircleAvatar(
                radius: 25,
                backgroundImage: (player.imageUrl != null &&
                    player.imageUrl!.isNotEmpty)
                    ? NetworkImage(player.imageUrl!)
                    : const AssetImage('assets/default_player.png')
                as ImageProvider,
              ),
              title: Text(
                player.nom ?? ' ',
                style: const TextStyle(
                    fontFamily: 'Roboto',
                    fontSize: 16,
                    fontWeight: FontWeight.bold),
              ),
              subtitle: Text(
                '${player.position ?? '-'}    ${player.goals ?? 0} ',
                style: const TextStyle(),
              ),
              trailing:
              const Icon(Icons.star, color: Colors.amber),
              onTap: () {
                // open player details if you want:
                Navigator.push(context, MaterialPageRoute(builder: (_) => FootballerDetailsScreen(playerId: player.id!)));
              },
            ),
          );
        },
      ),
    ));
  }
}


